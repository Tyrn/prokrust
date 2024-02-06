import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

class CompareFiles {
    companion object : Comparator<String> {
        override fun compare(a: String, b: String): Int {
            val (x, y) = if (opt.reverse) b to a else a to b
            return if (opt.sortLex) x.compareTo(y) else x.toPath().stem.compareToNaturally(y.toPath().stem)
        }
    }
}

class ComparePaths {
    companion object : Comparator<String> {
        override fun compare(a: String, b: String): Int {
            val (x, y) = if (opt.reverse) b to a else a to b
            return if (opt.sortLex) x.compareTo(y) else x.compareToNaturally(y)
        }
    }
}

/**
 * The file isn't to be ignored.
 * @receiver String
 */
fun String.isEligible(): Boolean {
    if (opt.fileType == null) return true
    val e = opt.fileType ?: ""
    val name = this.toPath().name

    if ('*' in e || '[' in e || ']' in e || '?' in e)
        return name.matches(e.replace("*", ".*").toRegex())

    return this.toPath().suffix.uppercase().trim('.') == e.uppercase().trim('.')
}

val knownExtensions =
    arrayOf(".MP3", ".OGG", ".M4A", ".M4B", ".OPUS", ".WMA", ".FLAC", ".APE", ".WAV")

/**
 * This name has an audio file extension.
 * @receiver String
 * @return this name has an audio file extension.
 */
fun String.isAudioFileExt(): Boolean {
    return this.isEligible() && knownExtensions
        .any { this.toPath().suffix.uppercase() == it }
}

data class FirstPass(
    val log: Sequence<String> = sequenceOf(),
    val tracks: Int = 0,
    val bytes: Long = 0L
)

operator fun FirstPass.plus(b: FirstPass): FirstPass =
    FirstPass(this.log + b.log, this.tracks + b.tracks, this.bytes + b.bytes)

/**
 * Walks down the directory tree.
 * @receiver Path
 * @return The sequence of FirstPass file attributes.
 */
fun Path.walk(): Sequence<FirstPass> {
    val (dirs, files) = this.listLazy()
    return (
            dirs.map { it.name }.flatMap { directory -> (this / directory).walk() }
                    + files.filter { it.toString().isAudioFileExt() }
                .map { file ->
                    FirstPass(
                        if (file.toString().isAudioFileExt()) sequenceOf()
                        else sequenceOf(" ${Icon.warning} Boo!"),
                        1,
                        file.size
                    )
                }
            )
}

/**
 * A [file] with its corresponding [stepsDown] list.
 */
data class FileTreeLeaf(val stepsDown: List<String>, val file: Path)

/**
 * Walks down the directory tree, accumulating
 * [stepsDown] on each recursion level.
 * @receiver Path
 * @return the sequence of all files,
 * each with its corresponding [stepsDown] list.
 */
fun Path.walk(stepsDown: List<String>): Sequence<FileTreeLeaf> {
    val (dirs, files) = this.listLazy()

    fun walkInto(dirs: Sequence<Path>) =
        dirs.map { it.name }.sortedWith(ComparePaths).flatMap { directory ->
            (this / directory).walk(stepsDown + directory)
        }

    fun walkAlong(files: Sequence<Path>) =
        files.map { it.name }.filter { it.isAudioFileExt() }.sortedWith(CompareFiles)
            .map { FileTreeLeaf(stepsDown, it.toPath()) }

    return if (opt.reverse) walkAlong(files) + walkInto(dirs)
    else walkInto(dirs) + walkAlong(files)
}

fun artistPart(forwDash: Boolean): String =
    if (opt.artist != null)
        if (forwDash) " - ${opt.artist}"
        else "${opt.artist} - "
    else ""

fun dstCalculate(): Path {
    val prefix = if (opt.albumNum != null) "${opt.albumNum?.toString(2, '0')}-"
    else ""
    val baseDst = prefix + if (opt.unifiedName != null) "${artistPart(false)}${opt.unifiedName}"
    else opt.src.toPath().name
    return if (opt.dropDst) opt.dst.toPath() else opt.dst.toPath() / baseDst
}

fun Path.fileCopyAndSetTags(i: Int, dst: Path) {
    this.fileCopy(dst)
}

fun FileTreeLeaf.trackCopy(i: Int, dst: Path, total: FirstPass) {
    val depth = if (opt.treeDst) this.stepsDown else listOf()
    val dstDir = dst.join(depth)
    FileSystem.SYSTEM.createDirectories(dstDir, false)

    val source = opt.src.toPath().join(this.stepsDown).div(this.file)
    val destination = dstDir / this.file

    source.fileCopyAndSetTags(i, destination)

    if (opt.verbose) {
        show(
            "${i.toString(total.tracks.toString().length)}/${total.tracks} ${Icon.column} $destination",
            false
        )
        val increase = destination.size - source.size
        if (increase > 0L) show(" ${Icon.column} +$increase", false)
        else if (increase < 0L) show(" ${Icon.column} $increase", false)
        show("")
    } else show(".", false)
}

fun albumCopy(start: Instant, total: FirstPass) {
    inline fun norm(i: Int) = if (opt.reverse) total.tracks - i else i + 1

    val dst = dstCalculate()
    FileSystem.SYSTEM.createDirectory(dst, false)

    if (!opt.verbose)
        show(" ${Icon.start} ", false)

    var secondPassTracks = 0
    opt.src.toPath().walk(listOf()).forEach { srcTreeLeaf ->
        srcTreeLeaf.trackCopy(norm(secondPassTracks), dst, total)
        secondPassTracks++
    }

    if (!opt.verbose)
        show(" ${Icon.stop}")

    if (total.tracks != secondPassTracks)
        throw RuntimeException("Track count, 1: ${total.tracks}; 2: $secondPassTracks")

    show(" ${Icon.done} Done (${total.tracks}, ${total.bytes.humanBytes}; ${Clock.System.stop(start)})")
}

fun appMain() {
    val start = Clock.System.now()
    var total: FirstPass? = null

    session {
        section {
            text("Checking... ")
            if (total != null) {
                val tot = total!!
                if (tot.tracks > 0)
                    if (opt.count) {
                        text("Valid: ${tot.tracks} file(s);")
                        text(" Volume: ${tot.bytes.humanBytes};")
                        if (tot.tracks > 1)
                            text(" Average: ${(tot.bytes / tot.tracks).humanBytes};")
                        text(" Time: ${Clock.System.stop(start)}")
                    } else text("Done in ${Clock.System.stop(start)}")
                else text("No audio files found")
            }
        }.run {
            total = opt.src.toPath().walk()
                .fold(FirstPass()) { acc, i -> acc + i }
            rerender()
        }
    }

    if (!opt.count && total!!.tracks > 0) albumCopy(start, total!!)

    total!!.log.forEach { show(it) }
}

fun show(str: String, trailingNewLine: Boolean = true) {
    opt.echo(str, trailingNewLine)
}

val opt = Prokrust()

fun main(args: Array<String>) = opt.main(args)
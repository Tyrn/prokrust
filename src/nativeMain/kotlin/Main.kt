import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.text
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
 * Only [this] kind of file is to be considered a valid audio file.
 * Accepts all files if the fileType option isn't specified.
 * @receiver String
 */
fun String.isEligible(): Boolean {
    if (opt.fileType == null) return true
    val e = opt.fileType ?: ""
    val name = this.toPath().name

    if ('*' in e || '[' in e || ']' in e || '?' in e)
    // This is an inadequate translation from glob to regex, to be improved later.
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

/**
 * First pass statistics unit, to be reduced or folded.
 */
data class FirstPass(
    val log: Sequence<String> = sequenceOf(),
    val tracks: Int = 0,
    val bytes: Long = 0L
)

/**
 * Calculates a sum of [this] and [b].
 * @receiver FirstPass
 * @return the sum of [this] and [b].
 */
operator fun FirstPass.plus(b: FirstPass): FirstPass =
    FirstPass(this.log + b.log, this.tracks + b.tracks, this.bytes + b.bytes)

/**
 * Walks down [this] directory tree, first pass.
 * @receiver Path
 * @return The sequence of FirstPass file attribute sets.
 */
fun Path.walk(): Sequence<FirstPass> {
    val (dirs, files) = this.listLazy()
    return (
            dirs.map { it.name }.flatMap { directory -> (this / directory).walk() }
                    + files.filter { it.toString().isAudioFileExt() }
                .map { file ->
                    FirstPass(
                        if (file.toString().isAudioFileExt()) sequenceOf()
                        else sequenceOf(" ${Icon.warning} $file"),
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
 * Walks down [this] directory tree, second pass,
 * accumulating [stepsDown] list of directory names
 * on each recursion level.
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

/**
 * Creates the artist part for a directory or file name to be decorated.
 * @return the artist name according to the command line options.
 */
fun artistPart(prefix: String = "", suffix: String = ""): String =
    if (opt.artist != null)
        "$prefix${opt.artist}$suffix"
    else ""

/**
 * Calculates the destination directory path
 * according to the command line options.
 * Does NOT create the destination directory.
 * @return the destination directory path (to be created).
 */
fun dstCalculate(): Path {
    val prefix = if (opt.albumNum != null) "${opt.albumNum?.toString(2, '0')}-"
    else ""
    val baseDst =
        prefix + if (opt.unifiedName != null) "${artistPart(suffix = " - ")}${opt.unifiedName}"
        else opt.src.toPath().name
    return if (opt.dropDst) opt.dst.toPath() else opt.dst.toPath() / baseDst
}

/**
 * Copies [this] file to [dst] file. Sets tags.
 */
fun Path.fileCopyAndSetTags(i: Int, dst: Path) {
    this.fileCopy(dst)
}

/**
 * Decorates [this] file name according to the command line options.
 * @receiver Path
 * @return a decorated file name with extension.
 */
fun Path.decorate(i: Int, stepsDown: List<String>, sumTotal: FirstPass): String {
    if (opt.stripDecorations && opt.treeDst) return this.name

    val prefix = i.toString(sumTotal.tracks.toString().length, '0') +
            if (opt.prependSubdirName && !opt.treeDst && stepsDown.isNotEmpty())
                "-[" + stepsDown.joinToString("][") + "]-"
            else "-"

    return prefix + if (opt.unifiedName != null) opt.unifiedName + artistPart(prefix = " - ") + this.suffix
    else this.name
}

/**
 * Copies the track number [i] to [dst] directory
 * according to the command line options and
 * [sumTotal] statistics acquired on the first pass
 * @receiver FileTreeLeaf
 */
fun FileTreeLeaf.trackCopy(i: Int, dst: Path, sumTotal: FirstPass) {
    val depth = if (opt.treeDst) this.stepsDown else listOf()
    val dstDir = dst.join(depth)

    val source = opt.src.toPath().join(this.stepsDown).div(this.file)
    val destination = dstDir / this.file.decorate(i, this.stepsDown, sumTotal)

    if (!opt.dryRun) {
        FileSystem.SYSTEM.createDirectories(dstDir, false)
        source.fileCopyAndSetTags(i, destination)
    }

    if (opt.verbose) {
        show(
            "${i.toString(sumTotal.tracks.toString().length)}/${sumTotal.tracks} ${Icon.column} $destination",
            false
        )
        if (!opt.dryRun) {
            val increase = destination.size - source.size
            if (increase > 0L) show(" ${Icon.column} +$increase", false)
            else if (increase < 0L) show(" ${Icon.column} $increase", false)
            show("")
        } else show(" ${Icon.column} ${source.size.humanBytes}")
    } else show(".", false)
}

/**
 * Copies the audio album in its entirety (implements the second pass),
 * according to the command line options.
 * Uses the [start] time mark and the [sumTotal] first pass
 * statistics to format the console output.
 */
fun albumCopy(start: Instant, sumTotal: FirstPass) {
    inline fun norm(i: Int) = if (opt.reverse) sumTotal.tracks - i else i + 1

    val dst = dstCalculate()
    if (!opt.dryRun)
        FileSystem.SYSTEM.createDirectory(dst, false)

    if (!opt.verbose)
        show(" ${Icon.start} ", false)

    var secondPassTracks = 0
    opt.src.toPath().walk(listOf()).forEach { srcTreeLeaf ->
        srcTreeLeaf.trackCopy(norm(secondPassTracks), dst, sumTotal)
        secondPassTracks++
    }

    if (!opt.verbose)
        show(" ${Icon.stop}")

    if (sumTotal.tracks != secondPassTracks)
        throw RuntimeException("Track count, 1: ${sumTotal.tracks}; 2: $secondPassTracks")

    show(
        " ${Icon.done} Done (${sumTotal.tracks}, ${sumTotal.bytes.humanBytes}; ${
            Clock.System.stop(
                start
            )
        })"
    )
}

/**
 * The application main function, to be called by the
 * Clickt command line parser.
 */
fun appMain() {
    val start = Clock.System.now()
    var sumTotal: FirstPass? = null

    session {
        section {
            text("Checking... ")
            if (sumTotal != null) {
                val sum = sumTotal!!
                if (sum.tracks > 0)
                    if (opt.count) {
                        text("Valid: ${sum.tracks} file(s);")
                        text(" Volume: ${sum.bytes.humanBytes};")
                        if (sum.tracks > 1)
                            text(" Average: ${(sum.bytes / sum.tracks).humanBytes};")
                        text(" Time: ${Clock.System.stop(start)}")
                    } else text("Done in ${Clock.System.stop(start)}")
                else text("No audio files found")
            }
        }.run {
            sumTotal = opt.src.toPath().walk()
                .fold(FirstPass()) { acc, i -> acc + i }
            rerender()
        }
    }

    val log = sumTotal!!.log + if (opt.count || !opt.dst.toPath().absolute
            .startsWith(opt.src.toPath().absolute)
    )
        sequenceOf()
    else {
        val dstMsg = " ${Icon.warning} Target directory \"${opt.dst.toPath().absolute}\""
        val srcMsg = " ${Icon.warning} is inside source \"${opt.src.toPath().absolute}\""
        if (opt.dryRun) sequenceOf(dstMsg, srcMsg, " ${Icon.warning} It won't run.")
        else {
            show(dstMsg)
            show(srcMsg)
            show(" ${Icon.warning} No go.")
            return
        }
    }

    if (!opt.count && sumTotal!!.tracks > 0) albumCopy(start, sumTotal!!)

    log.forEach { show(it) }
}

/**
 * Outputs [str] to the console.
 */
fun show(str: String, trailingNewLine: Boolean = true) {
    opt.echo(str, trailingNewLine)
}

/**
 * The global namespace of the Clickt command line parser.
 * Exposes its API and all the user-defined command line options (e.g. opt.verbose).
 */
val opt = Prokrust()

fun main(args: Array<String>) = opt.main(args)
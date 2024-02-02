import kotlinx.datetime.Clock
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

val knownExtensions =
    arrayOf(".MP3", ".OGG", ".M4A", ".M4B", ".OPUS", ".WMA", ".FLAC", ".APE", ".WAV")

/**
 * This name has an audio file extension.
 * @receiver String
 * @return this name has an audio file extension.
 */
fun String.isAudioFileExt(): Boolean {
    return knownExtensions
        .any { this.toPath().suffix.uppercase() == it }
}

/**
 * Walks down the directory tree.
 * @receiver Path
 * @return The sequence of all files.
 */
fun Path.walk(): Sequence<String> {
    val (dirs, files) = dirsAndFilesLazyPosix(this.toString())
    return (
            dirs.flatMap { directory -> (this / directory).walk() }
                    + files.filter { it.isAudioFileExt() }
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
    val (dirs, files) = dirsAndFilesLazyPosix(this.toString())

    fun walkInto(dirs: Sequence<String>) =
        dirs.sortedWith(ComparePaths).flatMap { directory ->
            (this / directory).walk(stepsDown + directory)
        }

    fun walkAlong(files: Sequence<String>) =
        files.filter { it.isAudioFileExt() }.sortedWith(CompareFiles)
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

fun FileTreeLeaf.trackCopy(i: Int, dst: Path, tracksTotal: Int) {
    val destination = dst.join(this.stepsDown)
    FileSystem.SYSTEM.createDirectories(destination, false)

    opt.src.toPath().join(this.stepsDown).div(this.file)
        .fileCopyAndSetTags(i, destination / this.file)

    show("${i.toString(tracksTotal.toString().length)}/$tracksTotal ${destination / this.file}")
}

fun albumCopy(tracksTotal: Int) {
    inline fun norm(i: Int) = if (opt.reverse) tracksTotal - i else i + 1

    val dst = dstCalculate()
    FileSystem.SYSTEM.createDirectory(dst, false)

    opt.src.toPath().walk(listOf()).forEachIndexed { i, srcTreeLeaf ->
        srcTreeLeaf.trackCopy(norm(i), dst, tracksTotal)
    }
}

fun appMain() {
    val start = Clock.System.now()
    val tracksTotal = opt.src.toPath().walk()
        .map { 1 }
        .sum()

    albumCopy(tracksTotal)

    show("Time: ${Clock.System.stop(start)}")
}

fun show(str: String) {
    opt.echo(str)
}

val opt = Prokrust()

fun main(args: Array<String>) = opt.main(args)
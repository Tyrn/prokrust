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
    val (dirs, files) = dirsAndFilesPairPosix(this.toString())
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
    val (dirs, files) = dirsAndFilesPairPosix(this.toString())

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

fun appMain() {
    val filesTotal = opt.src.toPath().walk()
        .map { 1 }
        .sum()
    opt.src.toPath().walk(listOf()).forEachIndexed { index, element ->
        show("${if (opt.reverse) filesTotal - index else index + 1}/$filesTotal ${element.stepsDown} ${element.file}")
    }
}

fun show(str: String) {
    opt.echo(str)
}

val opt = Prokrust()

fun main(args: Array<String>) = opt.main(args)
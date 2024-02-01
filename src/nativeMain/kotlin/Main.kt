import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import okio.Path
import okio.Path.Companion.toPath
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.min

/**
 * Makes a sequence of integers,
 * embedded in [this] argument.
 * @return sequence of integers to be read from left to right.
 */
inline fun String.stripNumbersLazy(): Sequence<Int> {
    return Reg.numbers.findAll(this)
        .map { it.value.toInt() }
}

/**
 * Compares [this] to [other] integer sequences using "string semantics",
 * consumes the sequences.
 * @return lt | eq | gt.
 */
inline fun Sequence<Int>.compareTo(other: Sequence<Int>): Int {
    return this
        .zip(other) { x, y -> x.compareTo(y) }
        .firstOrNull { it != 0 }
        ?: if (!this.iterator().hasNext())
            if (!other.iterator().hasNext()) 0 else -1
        else 1
}

/**
 * Makes a vector of integers,
 * embedded in [this] argument.
 * @return vector of integers read from left to right.
 */
inline fun String.stripNumbers(): IntArray {
    return Reg.numbers.findAll(this)
        .map { it.value.toInt() }
        .toList()
        .toIntArray()
}

/**
 * Compares [this] to [other] integer arrays using "string semantics".
 * @return lt | eq | gt.
 */
inline fun IntArray.compareTo(other: IntArray): Int {
    return this
        .zip(other) { x, y -> x.compareTo(y) }
        .firstOrNull { it != 0 }
        ?: (this.size - other.size)
}

/**
 * The sequence isn't spent.
 * An ad hoc drop-in replacement for List.isNotEmpty().
 * @receiver Sequence
 * @return the sequence is still iterable.
 */
inline fun Sequence<Int>.isNotEmpty(): Boolean = this.iterator().hasNext()

/**
 * If both [this] and [other] contain digits, returns numerical comparison based on the numeric
 * values embedded in the strings, otherwise returns the standard string comparison.
 * The idea of the natural sort as opposed to the standard lexicographic sort is one of coping
 * with the possible absence of the leading zeros in 'numbers' of files or directories.
 * @return lt | eq | gt
 */
inline fun String.compareToNaturally(other: String): Int {
    val nx = this.stripNumbersLazy()
    val ny = other.stripNumbersLazy()

    return if (nx.isNotEmpty() && ny.isNotEmpty()) nx.compareTo(ny)
    else this.compareTo(other)
}

class CompareFiles {
    companion object : Comparator<String> {
        override fun compare(a: String, b: String): Int {
            return if (opt.sortLex) a.compareTo(b) else a.toPath().stem.compareToNaturally(b.toPath().stem)
        }
    }
}

class ComparePaths {
    companion object : Comparator<String> {
        override fun compare(a: String, b: String): Int {
            return if (opt.sortLex) a.compareTo(b) else a.compareToNaturally(b)
        }
    }
}

val nobiliaryParticles = arrayOf(
    "von", "фон", "van", "ван", "der", "дер", "til", "тиль",
    "zu", "цу", "zum", "цум", "zur", "цур", "af", "аф",
    "of", "из", "da", "да", "de", "де", "des", "дез",
    "del", "дель", "di", "ди", "dos", "душ", "дос", "du", "дю",
    "la", "ла", "ля", "le", "ле", "haut", "от", "the",
)

/**
 * Reduces [authors] to initials.
 * @return the string with nice initials.
 */
fun initials(authors: String): String {
    fun formInitial(name: String): String {
        val cut = name.split("'")
        if (cut.size > 1 && cut[1].isNotEmpty()) {  // Deal with '.
            if (cut[1][0].isLowerCase() && cut[0].isNotEmpty()) {
                return cut[0][0].uppercase()
            }
            return cut[0] + "'" + cut[1][0]
        }
        if (name.length > 1) {
            when (name) {
                "Старший" -> return "Ст"
                "Младший" -> return "Мл"
                "Ст", "ст", "Sr", "Мл", "мл", "Jr" -> return name
            }
        }
        var head = name[0].toString()
        for (ch in name.slice(1..name.lastIndex)) {
            head += ch.toString()
            if (ch.isUpperCase()) return head
        }
        if (name in nobiliaryParticles) {
            return name[0].toString()
        }
        return name[0].toString().uppercase()
    }
    return authors
        .replace(Reg.quotedSubstrings, " ")
        .replace("\"", " ")
        .split(",")
        .filter { author -> author.replace(".", "").replace("-", "").trim().isNotEmpty() }
        .joinToString(",") { author ->
            author
                .split("-")
                .filter { barrel -> barrel.replace(".", "").trim().isNotEmpty() }
                .joinToString("-") { barrel ->
                    barrel
                        .split(Reg.dots)
                        .filter { name -> name.isNotEmpty() }
                        .joinToString(".") { name -> formInitial(name) }
                } + "."
        }
}

/**
 * Makes a human readable string representation
 * of [bytes], nicely rounded.
 * @return the rounded and annotated value.
 */
fun humanFine(bytes: Long): String {
    val unitList = arrayOf(
        { q: Float -> q.trim(0) },
        { q: Float -> "${q.trim(0)}kB" },
        { q: Float -> "${q.trim(1)}MB" },
        { q: Float -> "${q.trim(2)}GB" },
        { q: Float -> "${q.trim(2)}TB" },
        { q: Float -> "${q.trim(2)}PB" },
    )
    if (bytes > 1) {
        val exponent = min(
            a = log(bytes.toDouble(), 1024.toDouble()).toInt(),
            b = unitList.size - 1
        )
        val quotient = bytes / 1024.toDouble().pow(exponent)
        return unitList[exponent](quotient.toFloat())
    }
    if (bytes == 0L) return "0"
    if (bytes == 1L) return "1"
    throw RuntimeException("humanFine error; bytes: $bytes")
}

val useIcon = '\u2b50'
val crossIcon = '\u274c'
val whatIcon = '\u2754'

class Prokrust : CliktCommand(
    help =
    """
    Prokrust a.k.a. Damastes is a CLI utility for copying directories and subdirectories
    containing supported audio files in sequence, naturally sorted.
    The end result is a "flattened" copy of the source subtree. "Flattened" means
    that only a namesake of the root source directory is created, where all the files get
    copied to, names prefixed with a serial number. Tag "Track Number"
    is set, tags "Title", "Artist", and "Album" can be replaced optionally.
    The writing process is strictly sequential: either starting with the number one file,
    or in the reverse order. This can be important for some mobile devices.
    $crossIcon Broken media;
    $whatIcon Suspicious media;
    $useIcon Really useful options.

    Examples; <src> as a directory:

    robinson-crusoe $ prokrust -va 'Daniel "Goldeneye" Defoe' -m 'Robinson Crusoe' .
    /run/media/player

    <src> as a single file:

    library $ prokrust -va 'Vladimir Nabokov' -u 'Ada' ada.ogg .
    """,
) {
    val verbose by option(
        "-v", "--verbose", help = "${useIcon} Verbose output",
    ).flag()
    val dropTracknumber by option(
        "-d",
        "--drop-tracknumber",
        help = "Do not set track numbers",
    ).flag()
    val stripDecorations by option(
        "-s",
        "--strip-decorations",
        help = "Strip file and directory name decorations",
    ).flag()
    val fileTitle by option(
        "-f", "--file-title", help = "Use file name for title tag",
    ).flag()
    val fileTitleNum by option(
        "-F",
        "--file-title-num",
        help = "Use numbered file name for title tag",
    ).flag()
    val sortLex by option(
        "-x", "--sort-lex", help = "Sort files lexicographically",
    ).flag()
    val treeDst by option(
        "-t",
        "--tree-dst",
        help = "Retain the tree structure of the source album at destination",
    ).flag()
    val dropDst by option(
        "-p", "--drop-dst", help = "Do not create destination directory",
    ).flag()
    val reverse by option(
        "-r",
        "--reverse",
        help = "Copy files in reverse order (number one file is the last to be copied)",
    ).flag()
    val overwrite by option(
        "-w",
        "--overwrite",
        help = "Silently remove existing destination directory (not recommended)",
    ).flag()
    val dryRun by option(
        "-y",
        "--dry-run",
        help = "Without actually modifying anything (trumps -w, too)",
    ).flag()
    val count by option("-c", "--count", help = "Just count the files").flag()
    val prependSubdirName by option(
        "-i",
        "--prepend-subdir-name",
        help = "Prepend current subdirectory name to a file name",
    ).flag()
    val fileType by option(
        "-e",
        "--file-type",
        help = "Accept only specified audio files (e.g. -e flac, or even -e '*64kb.mp3')",
    )
    val unifiedName by option(
        "-u",
        "--unified-name",
        help = "${useIcon} <text> for everything unspecified",
    )
    val artist by option(
        "-a",
        "--artist",
        help = "${useIcon} Artist tag",
    )
    val album by option(
        "-m",
        "--album",
        help = "${useIcon} Album tag",
    )
    val albumNum by option(
        "-b",
        "--album-num",
        help = "0..99; prepend <int> to the destination root directory name",
    ).int()
    val src by argument("src").validate { require(it.isNotBlank()) { "Source file cannot be blank." } }
    val dst by argument("dst").validate { require(it.isNotBlank()) { "Destination cannot be blank." } }

    override fun run() {
        appMain()
    }
}

data class FileTreeLeaf(val stepsDown: List<String>, val file: Path)

fun dirWalk(stepsDown: List<String>, dir: Path): Sequence<FileTreeLeaf> {
    val (d, f) = dirsAndFilesPairPosix(dir.toString())
    val dirs = d.sortedWith(ComparePaths)
    val files = f.sortedWith(CompareFiles)

    fun walkInto(dirs: Sequence<String>): Sequence<FileTreeLeaf> {
        return dirs.flatMap { directory ->
            dirWalk(stepsDown + directory, dir / directory)
        }
    }

    fun walkAlong(files: Sequence<String>): Sequence<FileTreeLeaf> {
        return files.map { FileTreeLeaf(stepsDown, it.toPath()) }
    }
    return walkInto(dirs) + walkAlong(files)
}

fun appMain() {
    dirWalk(listOf(), opt.src.toPath()).forEach {
        show("${it.stepsDown} ${it.file}")
    }
}

fun show(str: String) {
    opt.echo(str)
}

val opt = Prokrust()

fun main(args: Array<String>) = opt.main(args)
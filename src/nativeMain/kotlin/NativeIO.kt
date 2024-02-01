import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.closedir
import platform.posix.dirent
import platform.posix.opendir
import platform.posix.readdir
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import platform.posix.S_IFDIR
import platform.posix.S_IFREG
import platform.posix.stat
import kotlin.experimental.ExperimentalNativeApi
import kotlin.math.roundToInt

@OptIn(ExperimentalNativeApi::class)
val fileSeparator = if (Platform.osFamily == OsFamily.WINDOWS) "\\" else "/"

object Reg {
    /**
     * A regular expression catching sequences of one or more digits.
     */
    val numbers = "\\d+".toRegex()

    /**
     * A regular expression catching file name without extension.
     */
    val stem = """(.*)\.[^.]*$""".toRegex()

    /**
     * A regular expression catching file name extension.
     */
    val suffix = """.*\.(.*)$""".toRegex()

    /**
     * A regular expression catching sequences of dots and space characters.
     */
    val dots = "[\\s.]+".toRegex()

    /**
     * A regular expression catching double-quoted substrings.
     */
    val quotedSubstrings = """"(?:\\.|[^"\\])*"""".toRegex()
}

/**
 * Walks the [parentDir] via cinterop.
 * @return a list of directories and files in [parentDir].
 */
@OptIn(ExperimentalForeignApi::class)
fun dirsAndFilesListPosix(parentDir: String): List<String> {
    val dp = opendir(parentDir) ?: throw RuntimeException("Couldn't open directory $parentDir")
    val entries = mutableListOf<String>()
    memScoped {
        var ep: CPointer<dirent>? = readdir(dp)
        while (ep != null) {
            val name = ep.pointed.d_name.toKString()
            if (name != "." && name != "..") {
                entries.add(name)
            }
            ep = readdir(dp)
        }
    }
    closedir(dp)
    return entries
}

/**
 * Walks the [parentDir] via cinterop.
 * @return a list of directories and a list of files in [parentDir].
 */
@OptIn(ExperimentalForeignApi::class)
fun dirsAndFilesPairPosix(parentDir: String): Pair<Sequence<String>, Sequence<String>> {
    val dp = opendir(parentDir) ?: throw RuntimeException("Couldn't open directory $parentDir")
    val dirs = mutableListOf<String>()
    val files = mutableListOf<String>()
    memScoped {
        var ep: CPointer<dirent>? = readdir(dp)
        while (ep != null) {
            val name = ep.pointed.d_name.toKString()
            if (name != "." && name != "..") {
                val path = "$parentDir$fileSeparator$name"
                val statBuf = alloc<stat>()
                if (stat(path, statBuf.ptr) == 0) {
                    if (statBuf.st_mode.toInt() and S_IFDIR != 0) {
                        dirs.add(name)
                    } else if (statBuf.st_mode.toInt() and S_IFREG != 0) {
                        files.add(name)
                    }
                }
            }
            ep = readdir(dp)
        }
    }
    closedir(dp)
    return Pair(dirs.asSequence(), files.asSequence())
}

/**
 * Copies [src] file to [dst]. If [dst] is an existing file,
 * it gets overwritten.
 */
fun fileCopy(src: Path, dst: Path) {
    FileSystem.SYSTEM.source(src).use { source ->
        FileSystem.SYSTEM.sink(dst).buffer().use { sink ->
            sink.writeAll(source)
        }
    }
}

/**
 * Checks path's prefix.
 * @receiver a path.
 * @return true, if [other] is a valid prefix of the receiver.
 */
fun Path.startsWith(other: Path): Boolean = normalized().run {
    other.normalized().let { normalizedOther ->
        normalizedOther.segments.size <= segments.size &&
                segments
                    .slice(0 until normalizedOther.segments.size)
                    .filterIndexed { index, s -> normalizedOther.segments[index] != s }
                    .isEmpty()
    }
}

/**
 * Extends a path with a list of directory [steps].
 * @receiver a path to be extended.
 * @return an extended path.
 */
fun Path.join(steps: List<String>): Path {
    return if (steps.isNotEmpty()) this / steps
        .map { it.toPath() }
        .reduce { acc, step -> acc / step }
    else this
}

/**
 * File name without extension.
 */
val Path.stem: String
    get() = Reg.stem.find(this.name)?.groupValues?.get(1) ?: this.name

/**
 * File name extension prefixed with a dot.
 */
val Path.suffix: String
    get() {
        val ext = Reg.suffix.find(this.name)?.groupValues?.get(1) ?: ""
        return if (ext.isNotEmpty()) ".$ext" else ext
    }

/**
 * Rounds a double to [decimals].
 * @receiver a double value to be rounded.
 * @return the rounded value.
 */
inline fun Double.roundToDecimals(decimals: Int): Double {
    var dotAt = 1
    repeat(decimals) { dotAt *= 10 }
    val roundedValue = (this * dotAt).roundToInt()
    return (roundedValue / dotAt) + (roundedValue % dotAt).toDouble() / dotAt
}

/**
 * Formats a double to [precision] decimal points.
 * @receiver a double value to be rounded and converted to String.
 * @return the rounded and trimmed value.
 */
fun Double.trim(precision: Int): String {
    val parts = this.roundToDecimals(precision).toString().split(".")
    if (precision == 0) return parts[0]
    return parts[0] + "." + (parts[1] + "0".repeat(precision)).slice(0..<precision)
}

/**
 * Converts Int to String with padding up to [width].
 * @receiver Int
 * @return the padded string representing the integer.
 */
fun Int.toString(width: Int, pad: Char = ' '): String {
    val str = this.toString()
    if (width < str.length) throw RuntimeException("Int.toString() extension: width $width too small")
    return pad.toString().repeat(width - str.length) + str
}
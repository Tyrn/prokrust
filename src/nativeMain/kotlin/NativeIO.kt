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
import okio.buffer
import okio.use
import platform.posix.S_IFDIR
import platform.posix.S_IFREG
import platform.posix.stat
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
val fileSeparator = if (Platform.osFamily == OsFamily.WINDOWS) "\\" else "/"

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

@OptIn(ExperimentalForeignApi::class)
fun dirsAndFilesPairPosix(parentDir: String): Pair<List<String>, List<String>> {
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
    return Pair(dirs, files)
}

fun fileCopy(src: Path, dstDir: Path) {
    FileSystem.SYSTEM.source(src).use { source ->
        FileSystem.SYSTEM.sink(dstDir).buffer().use { sink ->
            sink.writeAll(source)
        }
    }
}

fun Path.startsWith(other: Path) = normalized().run {
    other.normalized().let { normalizedOther ->
        normalizedOther.segments.size <= segments.size &&
                segments
                    .slice(0 until normalizedOther.segments.size)
                    .filterIndexed { index, s -> normalizedOther.segments[index] != s }
                    .isEmpty()
    }
}
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.closedir
import platform.posix.dirent
import platform.posix.opendir
import platform.posix.readdir
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

@OptIn(ExperimentalForeignApi::class)
fun listFilesAndDirsPosix(dir: String): List<String> {
    val dp = opendir(dir) ?: throw RuntimeException("Couldn't open directory $dir")
    val entries = mutableListOf<String>()
    memScoped {
        var ep: CPointer<dirent>? = readdir(dp)
        while (ep != null) {
            entries.add(ep.pointed.d_name.toKString())
            ep = readdir(dp)
        }
    }
    closedir(dp)
    return entries
}

fun fileCopy(src: Path, dstDir: Path) {
    FileSystem.SYSTEM.source(src).use { source ->
        FileSystem.SYSTEM.sink(dstDir).buffer().use { sink ->
            sink.writeAll(source)
        }
    }
}
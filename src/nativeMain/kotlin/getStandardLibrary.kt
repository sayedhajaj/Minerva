import kotlinx.cinterop.*
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.windows.GetModuleFileNameW
import platform.windows.MAX_PATH
import platform.windows.WCHARVar

actual fun getStandardLibrary() = loadLibrary("standard_library\\boolean.minerva") +
        loadLibrary("standard_library\\integer.minerva") +
        loadLibrary("standard_library\\decimal.minerva") +
        loadLibrary("standard_library\\char.minerva") +
        loadLibrary("standard_library\\iterable.minerva") +
        loadLibrary("standard_library\\string.minerva") +
        loadLibrary("standard_library\\array.minerva") +
        loadLibrary("standard_library\\random.minerva") +
        loadLibrary("standard_library\\math.minerva")

fun getExecutableDirectory(): String {
    val bufferLength = MAX_PATH
    val buffer = nativeHeap.allocArray<WCHARVar>(bufferLength)
    val length = GetModuleFileNameW(null, buffer, bufferLength.convert())
    return if (length > 0u) {
        val path = buffer.toKStringFromUtf16()
        path.substringBeforeLast("\\")

    } else ""
}

fun loadLibrary(name: String): String {
    val executableDir = getExecutableDirectory()
    val libraryDir = "$executableDir\\$name"
    return loadSource(libraryDir)
}

actual fun loadSource(path: String): String {
    try {
        return FileSystem.SYSTEM.read(path.toPath()) {
            readUtf8()
        }
    } catch (e: Exception) {
        println("Error loading source file: $path")
        throw e
    }
}
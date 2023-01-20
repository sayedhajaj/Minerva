import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class MinervaCLI {

    fun loadFile(filePath: String): String {
        val bytes = Files.readAllBytes(Paths.get(filePath))
        return String(bytes, Charset.defaultCharset())
    }

    fun processArgs(args: Array<String>) {
        if (args.isEmpty()) {
            val reader = BufferedReader(InputStreamReader(System.`in`))
            while (true) {
                val compiler = MinervaCompiler(reader.readLine())
                compiler.interpret()
            }
        } else if(args[0] == "interpret") {
            val source = loadFile(args[1])
            val compiler = MinervaCompiler(source)
            compiler.interpret()
        }
    }
}
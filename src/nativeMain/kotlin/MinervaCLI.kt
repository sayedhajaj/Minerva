
class MinervaCLI {


    fun processArgs(args: Array<String>) {
        if (args.isEmpty()) {
            while (true) {
                val compiler = MinervaCompiler(readLine()!!)
                compiler.interpret()
            }
        } else if(args[0] == "interpret") {
            val source = loadSource(args[1])

            val compiler = MinervaCompiler(source)
            compiler.interpret()
        }
    }
}
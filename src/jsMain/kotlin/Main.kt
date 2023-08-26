import backend.treewalk.Interpreter
import backend.treewalk.MinervaCallable
import backend.treewalk.natives.MinervaInteger
import kotlinx.browser.document
import kotlinx.dom.clear
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement

fun main() {

    val blah = js("require('./editor.js')")

    document.getElementById("run")?.addEventListener("click", {

        val lines = blah.getEditorState() as Array<String>
        val code = lines.joinToString("")

        val compiler = MinervaCompiler(code)

        val canvas = document.getElementById("canvas") as HTMLCanvasElement
        canvas.width = canvas.width




        compiler.defineNative("getCanvas", object: MinervaCallable {
            override fun arity(): Int = 0

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return MinervaCanvas(canvas, interpreter)
            }
        })

        val output = compiler.interpret()
        println(output)

        val consoleDiv = document.getElementById("console") as HTMLDivElement
        consoleDiv.innerText = output.joinToString("\n")
    })
}

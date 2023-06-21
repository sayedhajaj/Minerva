import backend.treewalk.Interpreter
import backend.treewalk.MinervaCallable
import backend.treewalk.natives.MinervaInteger
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement


fun main() {
    document.getElementById("run")?.addEventListener("click", {
        val code = (document.getElementById("code") as HTMLTextAreaElement).value

        val compiler = MinervaCompiler(code)


        val output = compiler.interpret()
        println(output)

        val consoleDiv = document.getElementById("console") as HTMLDivElement
        consoleDiv.innerText = output.joinToString("\n")
    })
}

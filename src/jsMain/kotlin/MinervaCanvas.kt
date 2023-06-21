import backend.treewalk.Interpreter
import backend.treewalk.MinervaCallable
import backend.treewalk.MinervaFunction
import backend.treewalk.MinervaInstance
import backend.treewalk.natives.MinervaInteger
import frontend.Token
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

class MinervaCanvas(canvas: HTMLCanvasElement
, interpreter: Interpreter) : MinervaInstance(null, interpreter) {

    val context: CanvasRenderingContext2D  = (canvas.getContext("2d") as CanvasRenderingContext2D?)!!

    override fun get(name: Token): Any? {
        return when(name.lexeme) {
            "fillRect" -> object : MinervaCallable {
                override fun arity() = 4

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val x = (arguments[0] as MinervaInteger).value.toDouble()
                    val y = (arguments[1] as MinervaInteger).value.toDouble()
                    val width = (arguments[2] as MinervaInteger).value.toDouble()
                    val height = (arguments[3] as MinervaInteger).value.toDouble()

                    context.fillRect(x, y, width, height)
                    return null
                }
            }
            else -> null
        }
    }
}

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

            "drawRect" -> object : MinervaCallable {
                override fun arity() = 4

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val x = (arguments[0] as MinervaInteger).value.toDouble()
                    val y = (arguments[1] as MinervaInteger).value.toDouble()
                    val width = (arguments[2] as MinervaInteger).value.toDouble()
                    val height = (arguments[3] as MinervaInteger).value.toDouble()

                    context.strokeRect(x, y, width, height)

                    return null
                }
            }

            "beginPath" -> object : MinervaCallable {
                override fun arity() = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    context.beginPath()
                    return null
                }
            }

            "closePath" -> object : MinervaCallable {
                override fun arity() = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    context.closePath()
                    return null
                }
            }

            "fillPath" -> object : MinervaCallable {
                override fun arity() = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    context.fill()
                    return null
                }
            }

            "drawPath" -> object : MinervaCallable {
                override fun arity() = 0

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    context.stroke()
                    return null
                }
            }

            "moveTo" -> object : MinervaCallable {
                override fun arity() = 2

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val x = (arguments[0] as MinervaInteger).value.toDouble()
                    val y = (arguments[1] as MinervaInteger).value.toDouble()
                    context.moveTo(x, y)
                    return null
                }
            }

            "lineTo" -> object : MinervaCallable {
                override fun arity() = 2

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val x = (arguments[0] as MinervaInteger).value.toDouble()
                    val y = (arguments[1] as MinervaInteger).value.toDouble()
                    context.lineTo(x, y)
                    return null
                }
            }

            "quadraticCurveTo" -> object : MinervaCallable {
                override fun arity() = 4

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val xControl = (arguments[0] as MinervaInteger).value.toDouble()
                    val yControl = (arguments[1] as MinervaInteger).value.toDouble()
                    val xEnd = (arguments[2] as MinervaInteger).value.toDouble()
                    val yEnd = (arguments[3] as MinervaInteger).value.toDouble()

                    context.quadraticCurveTo(xControl, yControl, xEnd, yEnd)
                    return null
                }
            }

            "bezierCurveTo" -> object : MinervaCallable {
                override fun arity() = 4

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val xControl = (arguments[0] as MinervaInteger).value.toDouble()
                    val yControl = (arguments[1] as MinervaInteger).value.toDouble()

                    val xControl2 = (arguments[2] as MinervaInteger).value.toDouble()
                    val yControl2 = (arguments[3] as MinervaInteger).value.toDouble()

                    val xEnd = (arguments[4] as MinervaInteger).value.toDouble()
                    val yEnd = (arguments[5] as MinervaInteger).value.toDouble()

                    context.bezierCurveTo(xControl, yControl, xControl2, yControl2, xEnd, yEnd)
                    return null
                }
            }

            "arcTo" -> object : MinervaCallable {
                override fun arity() = 5

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val x1 = (arguments[0] as MinervaInteger).value.toDouble()
                    val y1 = (arguments[1] as MinervaInteger).value.toDouble()

                    val x2 = (arguments[2] as MinervaInteger).value.toDouble()
                    val y2 = (arguments[3] as MinervaInteger).value.toDouble()

                    val radius = (arguments[4] as MinervaInteger).value.toDouble()


                    context.arcTo(x1, y1, x2, y2, radius)
                    return null
                }
            }


            "setColor" -> object : MinervaCallable {
                override fun arity() = 4

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val r = (arguments[0] as MinervaInteger).value.toDouble()
                    val g = (arguments[1] as MinervaInteger).value.toDouble()
                    val b = (arguments[2] as MinervaInteger).value.toDouble()
                    val a = (arguments[3] as MinervaInteger).value.toDouble()

                    context.fillStyle = "rgba($r, $g, $b, $a)"
                    context.strokeStyle = "rgba($r, $g, $b, $a)"

                    return null
                }
            }

            else -> null
        }
    }
}

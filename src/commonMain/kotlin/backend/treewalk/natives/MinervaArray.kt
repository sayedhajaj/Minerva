package backend.treewalk.natives

import backend.treewalk.Interpreter
import backend.treewalk.MinervaCallable
import backend.treewalk.MinervaFunction
import backend.treewalk.MinervaInstance
import frontend.Token

class MinervaArray(val elements: Array<Any?>, interpreter: Interpreter) : MinervaInstance(null, interpreter) {

    override fun toString(): String {
        return elements.contentToString()
    }

    fun get(index: Int) : Any?  {
        return elements[index]
    }

    fun set(index: Int, value: Any?): Any? {
        elements[index] = value
        return value
    }

    override fun get(name: Token): Any? {
        return when (name.lexeme) {
            "map" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val mapper = arguments[0] as MinervaFunction
                    val newElements = elements.map {
                        mapper.call(interpreter, listOf(it))
                    }
                    return MinervaArray(newElements.toTypedArray(), interpreter)
                }
            }
            "reduce" -> object: MinervaCallable {
                override fun arity(): Int = 2

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val reducer = arguments[0] as MinervaFunction
                    var result = arguments[1];
                    var index = 0;
                    while (index < elements.size) {
                        val current = elements[index];
                        result = reducer.call(interpreter, listOf(result, current));
                        index += 1;
                    };
                    return result
                }
            }
            "get" -> object : MinervaCallable {
                override fun arity() = 1

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val index = arguments[0] as MinervaInteger
                    return elements[index.value]
                }


            }
            "set" -> object : MinervaCallable {
                override fun arity(): Int = 2

                override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                    val index = arguments[0] as MinervaInteger
                    val value = arguments[1]
                    elements[index.value] = value
                    return value
                }
            }
            "length" -> MinervaInteger(elements.size, interpreter)
            else -> null
        }
    }
}
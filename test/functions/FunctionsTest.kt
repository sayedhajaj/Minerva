package functions

import HelloWorldTest
import Minerva
import MinervaCompiler
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class FunctionsTest {
    @Test
    internal fun testFunctions() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/functions.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("Hello World!", "8", "9", "120"), output, "")
    }

    @Test
    internal fun testMutualRecursion() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/mutual_recursion.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("true", "true"), output, "")
    }

    @Test
    internal fun testDoubleCapture() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/double_capture.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("1"), output, "")
    }

    @Test
    internal fun testUseThenDefine() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/use_then_define.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("8"), output, "")
    }

    @Test
    internal fun testFunctionParameters() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/function_parameters.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("9"), output, "")
    }
}
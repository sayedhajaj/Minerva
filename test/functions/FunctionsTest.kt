package functions

import HelloWorldTest
import Minerva
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class FunctionsTest {
    @Test
    internal fun testFunctions() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/functions.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("Hello World!", "8", "9", "120"), output, "")
    }

    @Test
    internal fun testMutualRecursion() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/mutual_recursion.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("true", "true"), output, "")
    }

    @Test
    internal fun testDoubleCapture() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/double_capture.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("1"), output, "")
    }

    @Test
    internal fun testUseThenDefine() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/use_then_define.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("8"), output, "")
    }
}
package functions

import HelloWorldTest
import Minerva
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ClosureTest {
    @Test
    internal fun testCounter() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/closures/counter.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("1", "2"), output, "")
    }

    @Test
    internal fun testClosesOverFreeVariables() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/closures/closes_over_free_variables.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("global", "global"), output, "")
    }

    @Test
    internal fun testAdder() {
        val source = HelloWorldTest::class.java.getResource("examples/functions/closures/adder.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("8"), output, "")
    }
}
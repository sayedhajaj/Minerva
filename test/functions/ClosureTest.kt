package functions

import HelloWorldTest
import Minerva
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ClosureTest {
    @Test
    internal fun testCounter() {
        val source = HelloWorldTest::class.java.getResource("functions/closures/counter.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("1", "2"), output, "")
    }

    @Test
    internal fun testClosesOverFreeVariables() {
        val source = HelloWorldTest::class.java.getResource("functions/closures/closes_over_free_variables.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("global", "global"), output, "")
    }
}
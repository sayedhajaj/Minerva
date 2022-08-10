package controlFlow

import HelloWorldTest
import Minerva
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class TypeMatchTest {

    @Test
        internal fun testTypeMatch() {
        val source = HelloWorldTest::class.java.getResource("control_flow/typematch.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("I'm a string!", "I'm a number!"), output, "")
    }
}
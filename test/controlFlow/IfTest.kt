package controlFlow

import HelloWorldTest
import Minerva
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class IfTest {

    @Test
    internal fun testIf() {
        val source = HelloWorldTest::class.java.getResource("control_flow/if.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("You are an adult", "adult"), output, "")
    }
}
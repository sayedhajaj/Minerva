package controlFlow

import HelloWorldTest
import Minerva
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class IfTest {

    @Test
    internal fun testIf() {
        val source = HelloWorldTest::class.java.getResource("control_flow/if/if.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("You are an adult", "adult"), output, "")
    }

    @Test
    internal fun testDanglingElse() {
        val source = HelloWorldTest::class.java.getResource("control_flow/if/dangling_else.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("good"), output, "")
    }

}
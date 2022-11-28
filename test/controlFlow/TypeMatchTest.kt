package controlFlow

import HelloWorldTest
import Minerva
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class TypeMatchTest {

    @Test
        internal fun testTypeMatch() {
        val source = HelloWorldTest::class.java.getResource("examples/control_flow/typematch.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("I'm a string!", "I'm a number!", "huzzah!"), output, "")
    }
}
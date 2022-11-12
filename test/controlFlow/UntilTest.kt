package controlFlow

import HelloWorldTest
import Minerva
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class UntilTest {
    @Test
    internal fun testUntil() {
        val source = HelloWorldTest::class.java.getResource("examples/control_flow/until.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), output, "")
    }
}
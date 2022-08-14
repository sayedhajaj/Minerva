package expressions

import HelloWorldTest
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class Precedence {

    @Test
    internal fun testPrecedence() {
        val source = HelloWorldTest::class.java.getResource("expressions/precedence.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(
            arrayOf("14", "8", "4", "0", "true", "true", "true", "true", "0", "0", "0", "0", "4"),
            output,
            ""
        )
    }
}
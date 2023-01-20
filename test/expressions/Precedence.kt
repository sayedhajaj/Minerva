package expressions

import HelloWorldTest
import MinervaCompiler
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class Precedence {

    @Test
    internal fun testPrecedence() {
        val source = HelloWorldTest::class.java.getResource("examples/expressions/precedence.minerva").readText()

        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(
            arrayOf("14", "8", "4", "0", "true", "true", "true", "true", "0", "0", "0", "0", "4"),
            output,
            ""
        )
    }
}
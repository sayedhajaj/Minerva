package expressions

import HelloWorldTest
import MinervaCompiler
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class UnaryTest {

    @Test
    internal fun testUnary() {
        val source = HelloWorldTest::class.java.getResource("examples/expressions/unary.minerva").readText()

        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(
            arrayOf("10", "11", "10"),
            output,
            ""
        )
    }
}
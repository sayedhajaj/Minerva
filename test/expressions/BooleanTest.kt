package expressions

import HelloWorldTest
import MinervaCompiler
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class BooleanTest {

    @Test
    internal fun testBoolean() {
        val source = HelloWorldTest::class.java.getResource("examples/expressions/booleans.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(
            arrayOf(
                "true",
                "false",
                "false",
                "true",
                "false",
                "true",
                "true",
                "false",
                "false",
                "true",
                "true"
            ), output, ""
        )
    }
}
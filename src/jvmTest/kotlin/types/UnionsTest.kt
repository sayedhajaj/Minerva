package types

import HelloWorldTest
import MinervaCompiler
import org.junit.Test
import kotlin.test.assertContentEquals

class UnionsTest {
    @Test
    internal fun testStrings() {
        val source = HelloWorldTest::class.java.getResource("examples/types/unions.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("Bob"), output, "")
    }
}
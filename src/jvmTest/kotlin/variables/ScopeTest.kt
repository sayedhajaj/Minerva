package variables

import HelloWorldTest
import MinervaCompiler
import org.junit.Test
import kotlin.test.assertContentEquals

class ScopeTest {
    @Test
    internal fun testScope() {
        val source = HelloWorldTest::class.java.getResource("examples/variables/scope.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(
            arrayOf(
                "inner a",
                "outer b",
                "global c",
                "outer a",
                "outer b",
                "global c",
                "global a",
                "global b",
                "global c"
            ), output, ""
        )
    }
}
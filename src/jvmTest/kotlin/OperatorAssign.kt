
import kotlin.test.Test
import kotlin.test.assertContentEquals

class OperatorAssign {
    @Test
    internal fun testOperatorAssign() {
        val source = HelloWorldTest::class.java.getResource("examples/operator_assign.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("15", "13"), output, "")
    }
}
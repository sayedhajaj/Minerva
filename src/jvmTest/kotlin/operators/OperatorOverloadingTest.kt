package operators
import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class OperatorOverloadingTest {
    @Test
    internal fun testVector() {
        val source = HelloWorldTest::class.java.getResource("examples/operators/operator_overloading/vector.minerva").readText()
        val compiler = MinervaCompiler(source)
        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("35.0", "-30.0"), output, "")
    }

    @Test
    internal fun testComparison() {
        val source = HelloWorldTest::class.java.getResource("examples/operators/operator_overloading/comparison.minerva").readText()
        val compiler = MinervaCompiler(source)
        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("true"), output, "")
    }
}
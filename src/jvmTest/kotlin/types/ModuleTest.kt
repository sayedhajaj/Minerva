package types
import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ModuleTest {
    @Test
    internal fun testModules() {
        val source = HelloWorldTest::class.java.getResource("examples/modules/module.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("bar", "11", "4", "hello", "SubModule"), output, "")
    }

    @Test
    internal fun testExternalModule() {
        val source = HelloWorldTest::class.java.getResource("examples/modules/external_module.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("Example", "This is an example"), output, "")
    }
}
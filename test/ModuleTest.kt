import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ModuleTest {
    @Test
    internal fun testModules() {
        val source = HelloWorldTest::class.java.getResource("examples/module.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("bar", "11", "4", "hello", "SubModule"), output, "")
    }

    @Test
    internal fun testExternalModule() {
        val source = HelloWorldTest::class.java.getResource("examples/external_module.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("Example", "This is an example"), output, "")
    }
}
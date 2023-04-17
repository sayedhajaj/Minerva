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
}
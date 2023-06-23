
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class HelloWorldTest {
    @Test
    internal fun resolvesCorrectly() {
        val source = loadSource("examples/hello_world.minerva")
        val compiler = MinervaCompiler(source)
        val errors = compiler.frontEndPass().first

        assertTrue(errors.isEmpty())
    }

    @Test
    internal fun printsHelloWorld() {
        val source = loadSource("examples/hello_world.minerva")
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()

        assertContains(output, "Hello World!")
    }

}

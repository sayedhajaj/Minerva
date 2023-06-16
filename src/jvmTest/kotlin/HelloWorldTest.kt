import org.junit.Test
import kotlin.test.assertContains

class HelloWorldTest {
    @Test
    internal fun resolvesCorrectly() {
        val source = HelloWorldTest::class.java.getResource("examples/hello_world.minerva").readText()
        val compiler = MinervaCompiler(source)
        val errors = compiler.frontEndPass().first

        assert(errors.isEmpty())
    }

    @Test
    internal fun printsHelloWorld() {
        val source = HelloWorldTest::class.java.getResource("examples/hello_world.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()

        assertContains(output, "Hello World!")
    }
}

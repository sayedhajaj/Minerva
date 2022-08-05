import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class MatchTest {
    @Test
    internal fun testMatch() {
        val source = HelloWorldTest::class.java.getResource("match.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("8"), output, "expect fib of 5 to be 8")
    }
}
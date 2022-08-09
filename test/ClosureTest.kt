import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ClosureTest {
    @Test
    internal fun testClosures() {
        val source = HelloWorldTest::class.java.getResource("closures.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("1", "2"), output, "")
    }
}
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class WhileTest {

    @Test
    internal fun testWhile() {
        val source = HelloWorldTest::class.java.getResource("while.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), output, "")
    }
}
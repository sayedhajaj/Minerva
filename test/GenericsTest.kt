import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class GenericsTest {

    @Test
    internal fun testGenerics() {
        val source = HelloWorldTest::class.java.getResource("generics.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("6", "11", "3", "7", "18", "Container<String>"), output, "")
    }
}
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ArraysTest {
    @Test
    internal fun testArrays() {
        val source = HelloWorldTest::class.java.getResource("examples/arrays.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("4", "[2, 4, 6, 8]", "24", "10", "6", "3"), output, "")
    }
}
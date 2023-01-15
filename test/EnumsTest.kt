import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class EnumsTest {
    @Test
    internal fun testEnums() {
        val source = HelloWorldTest::class.java.getResource("examples/enums.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("North", "0"), output, "")
    }
}
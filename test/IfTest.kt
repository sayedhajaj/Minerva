import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals

class IfTest {

    @Test
    internal fun testIf() {
        val source = HelloWorldTest::class.java.getResource("if.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("You are an adult", "adult"), output, "")
    }
}
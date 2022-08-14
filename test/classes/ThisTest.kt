package classes

import HelloWorldTest
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ThisTest {
    @Test
    internal fun testClasses() {
        val source = HelloWorldTest::class.java.getResource("classes/this/closure.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("Foo"), output, "")
    }
}
package classes

import HelloWorldTest
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ThisTest {
    @Test
    internal fun testClosure() {
        val source = HelloWorldTest::class.java.getResource("classes/this/closure.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("Foo"), output, "")
    }

    @Test
    internal fun testNestedClosure() {
        val source = HelloWorldTest::class.java.getResource("classes/this/nested_closure.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("Foo"), output, "")
    }

    @Test
    internal fun testThisInMethod() {
        val source = HelloWorldTest::class.java.getResource("classes/this/this_in_method.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("baz"), output, "")
    }

}
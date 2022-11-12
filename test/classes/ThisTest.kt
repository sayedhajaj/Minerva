package classes

import HelloWorldTest
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ThisTest {
    @Test
    internal fun testClosure() {
        val source = HelloWorldTest::class.java.getResource("examples/classes/this/closure.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("Foo"), output, "")
    }

    @Test
    internal fun testNestedClosure() {
        val source = HelloWorldTest::class.java.getResource("examples/classes/this/nested_closure.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("Foo"), output, "")
    }

    @Test
    internal fun testThisInMethod() {
        val source = HelloWorldTest::class.java.getResource("examples/classes/this/this_in_method.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("baz"), output, "")
    }

}
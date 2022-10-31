package classes

import HelloWorldTest
import Minerva
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ClassesTest {
    @Test
    internal fun testClasses() {
        val source = HelloWorldTest::class.java.getResource("classes/classes.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("12", "14", "6", "20", "18"), output, "")
    }

    @Test
    internal fun testUseThenDefine() {
        val source = HelloWorldTest::class.java.getResource("classes/use_then_define.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("John", "Garfield"), output, "")
    }
}
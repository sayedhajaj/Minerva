package classes

import HelloWorldTest
import Minerva
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class InheritanceTest {

    @Test
    internal fun testInheritance() {
        val source = HelloWorldTest::class.java.getResource("classes/inheritance.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("Fry until golden brown.", "Pipe full of custard and coat with chocolate."), output, "")
    }
}
package classes

import HelloWorldTest
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class InterfacesTest {
    @Test
    internal fun testInterfaces() {
        val source = HelloWorldTest::class.java.getResource("examples/interfaces/basic_interfaces.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("Hello World!"), output, "")
    }

    @Test
    internal fun testUnimplimented() {
        val source = HelloWorldTest::class.java.getResource("examples/interfaces/unimplimented_method.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = typeChecker.typeErrors.toTypedArray()
        assertContentEquals(arrayOf("Cannot assign ConsoleLog to Loggable", "ConsoleLog is missing log, (String):Any"), output, "")
    }

    @Test
    internal fun testAcceptInterface() {
        val source = HelloWorldTest::class.java.getResource("examples/interfaces/accept_interface.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf("Huzzah!"), output, "")
    }

    @Test
    internal fun testGenericInterface() {
        val source = HelloWorldTest::class.java.getResource("examples/interfaces/generic_interface.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(arrayOf(), output, "")
    }
}
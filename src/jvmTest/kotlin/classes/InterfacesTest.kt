package classes

import HelloWorldTest
import MinervaCompiler
import org.junit.Test
import kotlin.test.assertContentEquals

class InterfacesTest {
    @Test
    internal fun testInterfaces() {
        val source = HelloWorldTest::class.java.getResource("examples/interfaces/basic_interfaces.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("Hello World!"), output, "")
    }

    @Test
    internal fun testUnimplimented() {
        val source = HelloWorldTest::class.java.getResource("examples/interfaces/unimplimented_method.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.frontEndPass().first.map { it.message }.toTypedArray()
        assertContentEquals(arrayOf("Cannot assign ConsoleLog to Loggable", "ConsoleLog is missing log, (String):Any"), output, "")
    }

    @Test
    internal fun testAcceptInterface() {
        val source = HelloWorldTest::class.java.getResource("examples/interfaces/accept_interface.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("Huzzah!"), output, "")
    }

    @Test
    internal fun testGenericInterface() {
        val source = HelloWorldTest::class.java.getResource("examples/interfaces/generic_interface.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("14", "10", "6"), output, "")
    }
}
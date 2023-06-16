package variables

import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class VariablesTest {
    @Test
    internal fun testVariables() {
        val source = HelloWorldTest::class.java.getResource("examples/variables/variables.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("hello", "world", "bar", "4"), output, "")
    }

    @Test
    internal fun testConstants() {
        val source = HelloWorldTest::class.java.getResource("examples/variables/constants.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("hello"), output, "")
    }

    @Test
    internal fun testIncompatibleAssign() {
        val source = HelloWorldTest::class.java.getResource("examples/variables/incompatible_assign.minerva").readText()
        val compiler = MinervaCompiler(source)

        val (compileErrors) = compiler.frontEndPass()
        assertEquals("Cannot assign Int to String", compileErrors[0].message)
    }
}
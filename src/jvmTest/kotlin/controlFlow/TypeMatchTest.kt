package controlFlow

import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class TypeMatchTest {

    @Test
        internal fun testTypeMatch() {
        val source = HelloWorldTest::class.java.getResource("examples/control_flow/typematch.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("I'm a string!", "I'm a number!", "huzzah!"), output, "")
    }
}
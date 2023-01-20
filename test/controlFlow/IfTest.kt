package controlFlow

import HelloWorldTest
import Minerva
import MinervaCompiler
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class IfTest {

    @Test
    internal fun testIf() {
        val source = HelloWorldTest::class.java.getResource("examples/control_flow/if/if.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("You are an adult", "adult"), output, "")
    }

    @Test
    internal fun testDanglingElse() {
        val source = HelloWorldTest::class.java.getResource("examples/control_flow/if/dangling_else.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("good"), output, "")
    }

}
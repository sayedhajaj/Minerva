package classes

import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test

import kotlin.test.assertContentEquals

class ThisTest {
    @Test
    internal fun testClosure() {
        val source = HelloWorldTest::class.java.getResource("examples/classes/this/closure.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("Foo"), output, "")
    }

    @Test
    internal fun testNestedClosure() {
        val source = HelloWorldTest::class.java.getResource("examples/classes/this/nested_closure.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("Foo"), output, "")
    }

    @Test
    internal fun testThisInMethod() {
        val source = HelloWorldTest::class.java.getResource("examples/classes/this/this_in_method.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("baz"), output, "")
    }

}
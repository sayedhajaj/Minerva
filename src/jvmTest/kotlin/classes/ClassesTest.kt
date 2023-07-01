package classes

import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ClassesTest {
    @Test
    internal fun testClasses() {
        val source = HelloWorldTest::class.java.getResource("examples/classes/classes.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("12", "14", "6", "20", "18"), output, "")
    }

    @Test
    internal fun testUseThenDefine() {
        val source = HelloWorldTest::class.java.getResource("examples/classes/use_then_define.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("John", "Garfield"), output, "")
    }

    @Test
    internal fun testConstructorFields() {
        val source = HelloWorldTest::class.java.getResource("examples/classes/constructor_fields.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("12"), output, "")
    }
}
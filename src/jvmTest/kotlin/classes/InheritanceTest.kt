package classes

import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class InheritanceTest {

    @Test
    internal fun testInheritance() {
        val source = HelloWorldTest::class.java.getResource("examples/classes/inheritance.minerva").readText()

        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("Fry until golden brown.", "Pipe full of custard and coat with chocolate."), output, "")
    }
}
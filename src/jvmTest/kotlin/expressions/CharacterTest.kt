package expressions

import HelloWorldTest
import MinervaCompiler
import org.junit.Test
import kotlin.test.assertContentEquals

class CharacterTest {

    @Test
    internal fun testCharacters() {
        val source = HelloWorldTest::class.java.getResource("examples/expressions/characters.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(
            arrayOf(
                "true",
                "a",
            ), output, ""
        )
    }
}
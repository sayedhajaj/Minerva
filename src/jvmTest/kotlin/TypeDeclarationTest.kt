
import kotlin.test.Test
import kotlin.test.assertContentEquals

class TypeDeclarationTest {
    @Test
    internal fun testTypeDeclaration() {
        val source = HelloWorldTest::class.java.getResource("examples/type_declaration.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("50"), output, "")
    }
}
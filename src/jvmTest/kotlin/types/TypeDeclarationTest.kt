package types
import HelloWorldTest
import MinervaCompiler
import kotlin.test.Test
import kotlin.test.assertContentEquals

class TypeDeclarationTest {
    @Test
    internal fun testTypeDeclaration() {
        val source = HelloWorldTest::class.java.getResource("examples/types/type_declaration.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("50"), output, "")
    }


    @Test
    internal fun testEvalExpr() {
        val source = HelloWorldTest::class.java.getResource("examples/types/expr.minerva").readText()
        val compiler = MinervaCompiler(source)

        val output = compiler.interpret().toTypedArray()
        assertContentEquals(arrayOf("6"), output, "")
    }


}
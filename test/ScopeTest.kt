import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ScopeTest {
    @Test
    internal fun testScope() {
        val source = HelloWorldTest::class.java.getResource("examples/scope.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(
            arrayOf(
                "inner a",
                "outer b",
                "global c",
                "outer a",
                "outer b",
                "global c",
                "global a",
                "global b",
                "global c"
            ), output, ""
        )
    }
}
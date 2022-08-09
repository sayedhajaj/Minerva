import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ScopeTest {
    @Test
    internal fun testScope() {
        val source = HelloWorldTest::class.java.getResource("scope.minerva").readText()

        val syntaxTree = Minerva.getSyntaxTree(source)

        val output = Minerva.interpret(syntaxTree).toTypedArray()
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
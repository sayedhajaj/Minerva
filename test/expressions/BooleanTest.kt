package expressions

import HelloWorldTest
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class BooleanTest {

    @Test
    internal fun testBoolean() {
        val source = HelloWorldTest::class.java.getResource("examples/expressions/booleans.minerva").readText()

        val (typeChecker, syntaxTree) = Minerva.frontEndPass(source)

        val output = Minerva.interpret(typeChecker, syntaxTree).toTypedArray()
        assertContentEquals(
            arrayOf(
                "true",
                "false",
                "false",
                "true",
                "false",
                "true",
                "true",
                "false",
                "false",
                "true",
                "true"
            ), output, ""
        )
    }
}
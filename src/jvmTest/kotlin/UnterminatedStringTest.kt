import frontend.parsing.Scanner
import org.junit.Test
import kotlin.test.assertEquals


class UnterminatedStringTest {
    @Test
    internal fun testUnterminatedString() {
        val source = HelloWorldTest::class.java.getResource("examples/unterminated_string.minerva").readText()
        val scanner = Scanner(source)
        scanner.scanTokens()
        val scanErrors = scanner.scannerErrors

        assertEquals(scanErrors[0].message, "Unterminated string")
    }
}
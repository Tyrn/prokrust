import kotlin.test.Test
import kotlin.test.assertEquals

class UnitTest {
    @Test
    fun testInitials() {
        assertEquals("Alfa", initials("Alfa"))
        assertEquals("A B C", rDots.replace("A. ..B..C", " "))
        assertEquals("A B C", rQuotedSubstrings.replace("A\"Dobby\"B\"Bobby\"C", " "))
    }
}
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class UnitTest {
    @Test
    fun testStrStripNumbers() {
        assertContentEquals(
            intArrayOf(11, 2, 144).toList(),
            "ab11cdd2k.144".stripNumbers().toList()
        )
        assertContentEquals(
            intArrayOf(847, 1, 42, 31).toList(),
            "847.1._42_.31-knot".stripNumbers().toList()
        )
        assertContentEquals(intArrayOf().toList(), "Ignacio Vazquez-Abrams".stripNumbers().toList())
    }

    @Test
    fun testCompare() {
        assertEquals(-2, "a".compareTo("c"))
        assertEquals(-1, "a".compareTo("aa"))
        assertEquals(-1, "abc".compareTo("ac"))

        assertEquals(true, "".compareToNaturally("") == 0)
        assertEquals(true, "2a".compareToNaturally("10a") < 0)
        assertEquals(true, "alfa".compareToNaturally("bravo") < 0)
    }

    @Test
    fun testSort() {
        val unsortedStrings = arrayOf("ab", "aa")
        // unsortedStrings.sort()
        unsortedStrings.sortWith { a, b -> a.compareTo(b) }
        assertContentEquals(arrayOf("aa", "ab"), unsortedStrings)

        val unsortedInts = arrayOf(2, 1)
        unsortedInts.sortWith { a, b -> a.compareTo(b) }
        assertContentEquals(arrayOf(1, 2), unsortedInts)

        val unsortedIntArrays = arrayOf(intArrayOf(1, 1, 1), intArrayOf(2, 1))
        unsortedIntArrays.sortWith { a, b -> a.compareTo(b) }
        // Assertion is counterproductive, but one can introspect compareTo().
    }

    @Test
    fun testInitials() {
        assertEquals("A B C", rDots.replace("A. ..B..C", " "))
        assertEquals("A B C", rQuotedSubstrings.replace("A\"Dobby\"B\"Bobby\"C", " "))
        assertEquals("", initials(""))
        assertEquals("", initials(" "))
        assertEquals("", initials(".. , .. "))
        assertEquals("", initials(" ,, .,"))
        assertEquals("L.", initials("l"))
        assertEquals("A.G.", initials(", a. g, "))
        assertEquals("I.V-A.,E.C.N.", initials("- , -I.V.-A,E.C.N-, ."))
        assertEquals("J.R.R.T.", initials("John ronald reuel Tolkien"))
        assertEquals("E.B.S.", initials("  e.B.Sledge "))
        assertEquals("A.C-G.", initials("Apsley Cherry-Garrard"))
        assertEquals("W.S-C-G.", initials("Windsor Saxe-\tCoburg - Gotha"))
        assertEquals("E.K-R.", initials("Elisabeth Kubler-- - Ross"))
        assertEquals("F-S.A-B.L.", initials("  Fitz-Simmons Ashton-Burke Leigh"))
        assertEquals("A.B.", initials("Arleigh \"31-knot\"Burke "))
        assertEquals("H.C.,K.P.", initials("Harry \"Bing\" Crosby, Kris \"Tanto\" Paronto"))
        assertEquals(
            "W.J.D.,M.C.G.",
            initials("William J. \"Wild Bill\" Donovan, Marta \"Cinta Gonzalez")
        )
        assertEquals("A.S.,B.S.", initials("a.s , - . ,b.s."))
        assertEquals("A.S.,B.S.", initials("A. Strugatsky, B...Strugatsky."))
        assertEquals("И.К.,Й.Н.", initials("Иржи Кропачек,, Йозеф Новотный"))
        assertEquals("Я.динА.,Ш.д'А.", initials("Язон динАльт, Шарль д'Артаньян"))
        assertEquals("C.d.B.d.C.d'A.", initials("Charles de Batz de Castelmore d'Artagnan"))
        assertEquals("M.D.M.,H.o.L.", initials("Mario Del Monaco, Hutchinson of London"))
        assertEquals("A.h.R.", initials("Anselm haut Rodric"))
        assertEquals("А.о.Р.", initials("Ансельм от Родрик"))
        assertEquals("L.W.DiC.", initials("Leonardo Wilhelm DiCaprio"))
        assertEquals("Л.В.д.К.", initials("леонардо вильгельм ди каприо"))
        assertEquals("K.z.S.", initials("kapitän zur see"))
        assertEquals("D.B.,G.v.R.", initials("De Beers, Guido van Rossum"))
        assertEquals("М.ф.Р.", initials("Манфред фон Рихтгофен"))
        assertEquals("A.J.d.P.", initials("Armand Jean du Plessis"))
        assertEquals("J.D.v.d.W.", initials("johannes diderik van der waals"))
        assertEquals("K.H.a.S.", initials("Karl Hård af Segerstad"))
        assertEquals("Ö.Ü.A.", initials("Österreich über alles"))
        assertEquals("J.E.d.S.", initials("José Eduardo dos Santos"))
        assertEquals("Gnda'K.", initials("Gnda'Ke"))
        assertEquals("G.", initials("gnda'ke"))
        assertEquals("G.", initials("gnda'"))
        assertEquals("'B.", initials("'Bravo"))
        assertEquals("'.", initials("'"))
        assertEquals("'B.", initials("'B"))
        assertEquals("'b.", initials("'b"))
        assertEquals("dA.", initials("dA"))
        assertEquals("DA.", initials("DA"))
        assertEquals("DA.", initials("DAMadar"))
        assertEquals("П.Ст.", initials("Плиний Старший"))
        assertEquals("P.t.E.", initials("Pliny the Elder"))
        assertEquals("П.Мл.", initials("Плиний Младший"))
        assertEquals("П.Мл.", initials("Плиний Мл."))
        assertEquals("G.S.P.Jr.", initials("George Smith Patton Jr."))
        assertEquals("Д.С.П.ст.", initials("Джордж Смит паттон ст"))
        assertEquals("R.Sr.", initials("Redington Sr"))
    }

    @Test
    fun testRoundToDecimals() {
        assertEquals(2.0f, 1.8f.roundToDecimals(0))
    }

    @Test
    fun testHumanFine() {
        assertEquals("0", humanFine(0))
        assertEquals("1", humanFine(1))
        assertEquals("2", humanFine(2))
        assertEquals("42", humanFine(42))
        assertEquals("2kB", humanFine(1800))
        assertEquals("117.7MB", humanFine(123456789))
        assertEquals("114.98GB", humanFine(123456789123))
        assertEquals("1kB", humanFine(1024))
        assertEquals("1.0MB", humanFine(1024.toDouble().pow(2).toLong()))
        assertEquals("1.00GB", humanFine(1024.toDouble().pow(3).toLong()))
        assertEquals("440.3MB", humanFine(1024.toDouble().pow(3).times(.43).toLong()))
        assertEquals("1.00TB", humanFine(1024.toDouble().pow(4).toLong()))
        assertEquals("440.32GB", humanFine(1024.toDouble().pow(4).times(.43).toLong()))
        assertEquals("1.00PB", humanFine(1024.toDouble().pow(5).toLong()))
    }
}
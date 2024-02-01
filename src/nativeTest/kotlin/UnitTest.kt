import okio.Path.Companion.toPath
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnitTest {
    @Test
    fun testStringStripNumbers() {
        assertContentEquals(
            intArrayOf(11, 2, 144).toList(),
            "ab11cdd2k.144".stripNumbersLazy().toList()
        )
        assertContentEquals(intArrayOf(11, 2, 144), "ab11cdd2k.144".stripNumbers())
        assertContentEquals(intArrayOf(847, 1, 42, 31), "847.1._42_.31-knot".stripNumbers())
        assertContentEquals(
            intArrayOf().toList(),
            "Ignacio Vazquez-Abrams".stripNumbersLazy().toList()
        )
        assertContentEquals(intArrayOf(), "Ignacio Vazquez-Abrams".stripNumbers())
    }

    @Test
    fun testCompare() {
        assertEquals(-2, "a".compareTo("c"))
        assertEquals(-1, "a".compareTo("aa"))
        assertEquals(-1, "abc".compareTo("ac"))

        assertEquals(true, "".compareToNaturally("") == 0)
        assertEquals(true, "".compareToNaturally("a") < 0)
        assertEquals(true, "2a".compareToNaturally("10a") < 0)
        assertEquals(true, "10a".compareTo("2a") < 0)
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
    }

    @Test
    fun testInitials() {
        // There are four delimiters: comma, hyphen, dot, and space.
        // initials() syntax philosophy: if a delimiter is
        // misplaced, it's ignored.
        assertEquals("A B C", Reg.dots.replace("A. ..B..C", " "))
        assertEquals("A B C", Reg.quotedSubstrings.replace("A\"Dobby\"B\"Bobby\"C", " "))
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

    @Test
    fun testPathJoin() {
        assertEquals("a/b".toPath(), "a".toPath() / "b")
        assertEquals("a/b/c/d".toPath(), "a".toPath().join(listOf("b/", "c/./d/")))
        assertEquals("a".toPath(), "a".toPath().join(listOf("")))
        assertEquals("a".toPath(), "a".toPath().join(listOf()))
    }

    @Test
    fun testPathName() {
        assertEquals("alfa.mp3", "bobby/alfa.mp3".toPath().name)
        assertEquals("alfa", "alfa".toPath().stem)
        assertEquals("alfa", "alfa.".toPath().stem)
        assertEquals("alfa", "alfa.mp3".toPath().stem)
        assertEquals("alfa.bravo", "bobby/alfa.bravo.mp3".toPath().stem)
        assertEquals(".mp3", "bobby/alfa.bravo.mp3".toPath().suffix)
        assertEquals("", "alfa".toPath().suffix)
    }

    @Test
    fun testToString() {
        val int = 2245
        val str = int.toString()
        assertEquals(int.toString(), int.toString(str.length))
        assertEquals(" $str", int.toString(str.length + 1))
        assertEquals("00$str", int.toString(str.length + 2, '0'))
    }

    @Test
    fun testStartsWith() {
        listOf(
            "/a/b/c/d" to "/a/b/c",
            "/a/b/c/d" to "/a/b/c/",
            "/A/B/C/D" to "/A/B/C",
            "/a/b/c/d" to "/a/b//c/",
            "/a/b/c/d" to "/a/b/../b/c",
            "/a/b/c/d" to "/a/../a/./b/../b///c",
            "\\a\\b\\c\\d" to "/a/../a/./b/../b///c",
            "/home/user/.config/test" to "/home/user",
            "/var/www/html/app" to "/var/www/html",
            "/home/user" to "/",
            "/" to "/",
            "////////////////////////" to "/",
            "/" to "////////////////////////",
            "/home/user" to "/home/user",
            "/home/user/./" to "/home/user",
            "/home/user" to "/home/user/./",
            "/./var" to "/var",
            "." to ".",
            "./" to ".",
            ".." to "..",
            "../.." to "../..",
            "./.." to "../.",
            "../." to "./..",
            "./../." to ".././.",
            "/." to "/.",
            "./" to ".",
            "/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z" to "/a/b/c",
            "/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a" to "/a/a/a"
        ).forEach { (pathString, otherPathString) ->
            assertTrue(
                pathString.toPath().startsWith(otherPathString.toPath()),
                "$pathString should start with $otherPathString"
            )
        }

        listOf(
            "/a/b/c" to "/a/b/c/d/",
            "/a/b/c/" to "/a/b/c/d",
            "/a/b/d/d" to "/a/b/c",
            "/a/b/d/d" to "/a/b/ce",
            "/a/b/ce" to "/a/b/c",
            "/a/b/c" to "/a/b/ce",
            "/abcd" to "/a/b/c/d",
            "/a/../b/c" to "/a/b/c",
            "/a/b/" to "/a/b//c",
            "/a/b/c/d" to "/a/b/../c",
            "/a/b/c/d" to "/a/./a/../b/./b///c",
            "/a/b/c" to "/c/b/a",
            "/a/a/a/a" to "/a/a/a/a/a",
            "\\a\\b\\d\\d" to "\\a\\b\\c",
            "\\a\\b\\d\\d" to "/a/b/c",
            "/home/user/.config/test" to "/home/user2",
            "/var/www/html/app" to "/var/local/www/html/app",
            "/home/user" to ".",
            "/" to "./",
            "/home/user" to "/home/user2",
            "/home/user/./" to "/home/user2",
            "/home/user2" to "/home/user/./",
            "../var" to "/var",
            "." to "..",
            "./" to "..",
            ".." to ".",
            "/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z" to "/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/z",
            "/a/a/a" to "/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a",
            "/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a/a" to "/A",
        ).forEach { (pathString, otherPathString) ->
            assertFalse(
                pathString.toPath().startsWith(otherPathString.toPath()),
                "$pathString should not start with $otherPathString"
            )
        }
    }
}
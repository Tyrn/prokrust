import okio.Path.Companion.toPath
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnitTest {
    @Test
    fun testSequence() {
        assertContentEquals(
            expected = listOf("a", "b", "c"),
            actual = (listOf("a", "b").asSequence() + listOf("c").asSequence()).toList()
        )
        assertContentEquals(
            expected = listOf("a", "b"),
            actual = (listOf("a", "b").asSequence() + sequenceOf()).toList()
        )
    }
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
        listOf(
            "" to "",
            " " to "",
            ".. , .. " to "",
            " ,, .," to "",
            "l" to "L.",
            ", a. g, " to "A.G.",
            "- , -I.V.-A,E.C.N-, ." to "I.V-A.,E.C.N.",
            "John ronald reuel Tolkien" to "J.R.R.T.",
            "  e.B.Sledge " to "E.B.S.",
            "Apsley Cherry-Garrard" to "A.C-G.",
            "Windsor Saxe-\tCoburg - Gotha" to "W.S-C-G.",
            "Elisabeth Kubler-- - Ross" to "E.K-R.",
            "  Fitz-Simmons Ashton-Burke Leigh" to "F-S.A-B.L.",
            "Arleigh \"31-knot\"Burke " to "A.B.",
            "Harry \"Bing\" Crosby, Kris \"Tanto\" Paronto" to "H.C.,K.P.",
            "William J. \"Wild Bill\" Donovan, Marta \"Cinta Gonzalez" to "W.J.D.,M.C.G.",
            "a.s , - . ,b.s." to "A.S.,B.S.",
            "A. Strugatsky, B...Strugatsky." to "A.S.,B.S.",
            "Иржи Кропачек,, Йозеф Новотный" to "И.К.,Й.Н.",
            "Язон динАльт, Шарль д'Артаньян" to "Я.динА.,Ш.д'А.",
            "Charles de Batz de Castelmore d'Artagnan" to "C.d.B.d.C.d'A.",
            "Mario Del Monaco, Hutchinson of London" to "M.D.M.,H.o.L.",
            "Anselm haut Rodric" to "A.h.R.",
            "Ансельм от Родрик" to "А.о.Р.",
            "Leonardo Wilhelm DiCaprio" to "L.W.DiC.",
            "леонардо вильгельм ди каприо" to "Л.В.д.К.",
            "kapitän zur see" to "K.z.S.",
            "De Beers, Guido van Rossum" to "D.B.,G.v.R.",
            "Манфред фон Рихтгофен" to "М.ф.Р.",
            "Armand Jean du Plessis" to "A.J.d.P.",
            "johannes diderik van der waals" to "J.D.v.d.W.",
            "Karl Hård af Segerstad" to "K.H.a.S.",
            "Österreich über alles" to "Ö.Ü.A.",
            "José Eduardo dos Santos" to "J.E.d.S.",
            "Gnda'Ke" to "Gnda'K.",
            "gnda'ke" to "G.",
            "gnda'" to "G.",
            "'Bravo" to "'B.",
            "'" to "'.",
            "'B" to "'B.",
            "'b" to "'b.",
            "dA" to "dA.",
            "DA" to "DA.",
            "DAMadar" to "DA.",
            "Плиний Старший" to "П.Ст.",
            "Pliny the Elder" to "P.t.E.",
            "Плиний Младший" to "П.Мл.",
            "Плиний Мл." to "П.Мл.",
            "George Smith Patton Jr." to "G.S.P.Jr.",
            "Джордж Смит паттон ст" to "Д.С.П.ст.",
            "Redington Sr" to "R.Sr.",
        ).forEach { (authors, initials) ->
            assertEquals(authors.initials, initials)
        }
    }

    @Test
    fun testRoundToDecimals() {
        assertEquals(2.0, 1.8.roundToDecimals(0))
    }

    @Test
    fun testHumanFine() {
        assertEquals("0", 0L.humanBytes)
        assertEquals("1", 1L.humanBytes)
        assertEquals("2", 2L.humanBytes)
        assertEquals("42", 42L.humanBytes)
        assertEquals("2kB", 1800L.humanBytes)
        assertEquals("117.7MB", 123456789L.humanBytes)
        assertEquals("114.98GB", 123456789123L.humanBytes)
        assertEquals("1kB", 1024L.humanBytes)
        assertEquals("1.0MB", 1024L.toDouble().pow(2).toLong().humanBytes)
        assertEquals("1.00GB", 1024L.toDouble().pow(3).toLong().humanBytes)
        assertEquals("440.3MB", 1024L.toDouble().pow(3).times(.43).toLong().humanBytes)
        assertEquals("1.00TB", 1024L.toDouble().pow(4).toLong().humanBytes)
        assertEquals("440.32GB", 1024L.toDouble().pow(4).times(.43).toLong().humanBytes)
        assertEquals("1.00PB", 1024L.toDouble().pow(5).toLong().humanBytes)
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
            "/a/b/c" to "/a/b/c/",
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

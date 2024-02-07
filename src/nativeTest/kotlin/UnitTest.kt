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
            "" to " ",
            "" to ".. , .. ",
            "" to " ,, .,",
            "L." to "l",
            "A.G." to ", a. g, ",
            "I.V-A.,E.C.N." to "- , -I.V.-A,E.C.N-, .",
            "J.R.R.T." to "John ronald reuel Tolkien",
            "E.B.S." to "  e.B.Sledge ",
            "A.C-G." to "Apsley Cherry-Garrard",
            "W.S-C-G." to "Windsor Saxe-\tCoburg - Gotha",
            "E.K-R." to "Elisabeth Kubler-- - Ross",
            "F-S.A-B.L." to "  Fitz-Simmons Ashton-Burke Leigh",
            "A.B." to "Arleigh \"31-knot\"Burke ",
            "H.C.,K.P." to "Harry \"Bing\" Crosby, Kris \"Tanto\" Paronto",
            "W.J.D.,M.C.G." to "William J. \"Wild Bill\" Donovan, Marta \"Cinta Gonzalez",
            "A.S.,B.S." to "a.s , - . ,b.s.",
            "A.S.,B.S." to "A. Strugatsky, B...Strugatsky.",
            "И.К.,Й.Н." to "Иржи Кропачек,, Йозеф Новотный",
            "Я.динА.,Ш.д'А." to "Язон динАльт, Шарль д'Артаньян",
            "C.d.B.d.C.d'A." to "Charles de Batz de Castelmore d'Artagnan",
            "M.D.M.,H.o.L." to "Mario Del Monaco, Hutchinson of London",
            "A.h.R." to "Anselm haut Rodric",
            "А.о.Р." to "Ансельм от Родрик",
            "L.W.DiC." to "Leonardo Wilhelm DiCaprio",
            "Л.В.д.К." to "леонардо вильгельм ди каприо",
            "K.z.S." to "kapitän zur see",
            "D.B.,G.v.R." to "De Beers, Guido van Rossum",
            "М.ф.Р." to "Манфред фон Рихтгофен",
            "A.J.d.P." to "Armand Jean du Plessis",
            "J.D.v.d.W." to "johannes diderik van der waals",
            "K.H.a.S." to "Karl Hård af Segerstad",
            "Ö.Ü.A." to "Österreich über alles",
            "J.E.d.S." to "José Eduardo dos Santos",
            "Gnda'K." to "Gnda'Ke",
            "G." to "gnda'ke",
            "G." to "gnda'",
            "'B." to "'Bravo",
            "'." to "'",
            "'B." to "'B",
            "'b." to "'b",
            "dA." to "dA",
            "DA." to "DA",
            "DA." to "DAMadar",
            "П.Ст." to "Плиний Старший",
            "P.t.E." to "Pliny the Elder",
            "П.Мл." to "Плиний Младший",
            "П.Мл." to "Плиний Мл.",
            "G.S.P.Jr." to "George Smith Patton Jr.",
            "Д.С.П.ст." to "Джордж Смит паттон ст",
            "R.Sr." to "Redington Sr",
        ).forEach { (initials, authors) ->
            assertEquals(initials, authors.initials)
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

import com.github.ajalt.clikt.core.CliktCommand
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Makes a vector of integers,
 * embedded in the [str] argument.
 * @return vector of integers read from left to right.
 */
fun strStripNumbers(str: String): IntArray {
    return "\\d+".toRegex().findAll(str)
        .map {it.value.toString().toInt()}
        .toList()
        .toIntArray()
}

val nobiliaryParticles = arrayOf(
    "von", "фон", "van", "ван", "der", "дер", "til", "тиль",
    "zu", "цу", "zum", "цум", "zur", "цур", "af", "аф",
    "of", "из", "da", "да", "de", "де", "des", "дез",
    "del", "дель", "di", "ди", "dos", "душ", "дос", "du", "дю",
    "la", "ла", "ля", "le", "ле", "haut", "от", "the",
)
val rDots = "[\\s.]+".toRegex()
val rQuotedSubstrings = """"(?:\\.|[^"\\])*"""".toRegex()

/**
 * Reduces [authors] to initials.
 * @return the string with nice initials.
 */
fun initials(authors: String): String {
    fun formInitial(name: String): String {
        val cut = name.split("'")
        if (cut.size > 1 && cut[1].isNotEmpty()) {  // Deal with '.
            if (cut[1][0].isLowerCase() && cut[0].isNotEmpty()) {
                return cut[0][0].uppercase()
            }
            return cut[0] + "'" + cut[1][0]
        }
        if (name.length > 1) {
            when (name) {
                "Старший" -> return "Ст"
                "Младший" -> return "Мл"
                "Ст", "ст", "Sr", "Мл", "мл", "Jr" -> return name
            }
        }
        var head = name[0].toString()
        for (ch in name.slice(1..name.lastIndex)) {
            head += ch.toString()
            if (ch.isUpperCase()) return head
        }
        if (name in nobiliaryParticles) {
            return name[0].toString()
        }
        return name[0].toString().uppercase()
    }
    return authors
        .replace(rQuotedSubstrings, " ")
        .replace("\"", " ")
        .split(",")
        .filter { author -> author.replace(".", "").replace("-", "").trim().isNotEmpty() }
        .joinToString(",") { author ->
            author
                .split("-")
                .filter { barrel -> barrel.replace(".", "").trim().isNotEmpty() }
                .joinToString("-") { barrel ->
                    barrel
                        .split(rDots)
                        .filter { name -> name.isNotEmpty() }
                        .joinToString(".") { name -> formInitial(name) }
                } + "."
        }
}

/**
 * Rounds a float to [decimals].
 * @receiver a float value to be rounded.
 * @return the rounded value.
 */
fun Float.roundToDecimals(decimals: Int): Float {
    var dotAt = 1
    repeat(decimals) { dotAt *= 10 }
    val roundedValue = (this * dotAt).roundToInt()
    return (roundedValue / dotAt) + (roundedValue % dotAt).toFloat() / dotAt
}

/**
 * Makes a human readable string representation
 * of [bytes], nicely rounded.
 * @return the rounded and annotated value.
 */
fun humanFine(bytes: Long): String {
    fun Float.fmt(precision: Int): String {
        val parts = this.roundToDecimals(precision).toString().split(".")
        if (precision == 0) return parts[0]
        return parts[0] + "." + (parts[1] + "0".repeat(precision)).slice(0..<precision)
    }

    val unitList = arrayOf(
        { q: Float -> q.fmt(0) },
        { q: Float -> "${q.fmt(0)}kB" },
        { q: Float -> "${q.fmt(1)}MB" },
        { q: Float -> "${q.fmt(2)}GB" },
        { q: Float -> "${q.fmt(2)}TB" },
        { q: Float -> "${q.fmt(2)}PB" },
    )
    if (bytes > 1) {
        val exponent = min(
            a = log(bytes.toDouble(), 1024.toDouble()).toInt(),
            b = unitList.size - 1
        )
        val quotient = bytes / 1024.toDouble().pow(exponent)
        return unitList[exponent](quotient.toFloat())
    }
    if (bytes == 0L) return "0"
    if (bytes == 1L) return "1"
    return "humanFine error; bytes: $bytes"
}

class Shoot : CliktCommand() {
    override fun run() {
        echo("¡Hola, Kitty!")
    }
}

fun main(args: Array<String>) = Shoot().main(args)
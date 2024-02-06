import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.log
import kotlin.math.min
import kotlin.math.pow

/**
 * Makes a sequence of integers,
 * embedded in [this] argument.
 * @return sequence of integers to be read from left to right.
 */
inline fun String.stripNumbersLazy(): Sequence<Int> {
    return Reg.numbers.findAll(this)
        .map { it.value.toInt() }
}

/**
 * Compares [this] to [other] integer sequences using "string semantics",
 * consumes the sequences.
 * @return lt | eq | gt.
 */
inline fun Sequence<Int>.compareTo(other: Sequence<Int>): Int {
    return this
        .zip(other) { x, y -> x.compareTo(y) }
        .firstOrNull { it != 0 }
        ?: if (!this.iterator().hasNext())
            if (!other.iterator().hasNext()) 0 else -1
        else 1
}

/**
 * Makes a vector of integers,
 * embedded in [this] argument.
 * @return vector of integers read from left to right.
 */
inline fun String.stripNumbers(): IntArray {
    return Reg.numbers.findAll(this)
        .map { it.value.toInt() }
        .toList()
        .toIntArray()
}

/**
 * Compares [this] to [other] integer arrays using "string semantics".
 * @return lt | eq | gt.
 */
inline fun IntArray.compareTo(other: IntArray): Int {
    return this
        .zip(other) { x, y -> x.compareTo(y) }
        .firstOrNull { it != 0 }
        ?: (this.size - other.size)
}

/**
 * The sequence isn't spent.
 * An ad hoc drop-in replacement for List.isNotEmpty().
 * @receiver Sequence
 * @return the sequence is still iterable.
 */
inline fun Sequence<Int>.isNotEmpty(): Boolean = this.iterator().hasNext()

/**
 * If both [this] and [other] contain digits, returns numerical comparison based on the numeric
 * values embedded in the strings, otherwise returns the standard string comparison.
 * The idea of the natural sort as opposed to the standard lexicographic sort is one of coping
 * with the possible absence of the leading zeros in 'numbers' of files or directories.
 * @return lt | eq | gt
 */
inline fun String.compareToNaturally(other: String): Int {
    val nx = this.stripNumbersLazy()
    val ny = other.stripNumbersLazy()

    return if (nx.isNotEmpty() && ny.isNotEmpty()) nx.compareTo(ny)
    else this.compareTo(other)
}

val nobiliaryParticles = arrayOf(
    "von", "фон", "van", "ван", "der", "дер", "til", "тиль",
    "zu", "цу", "zum", "цум", "zur", "цур", "af", "аф",
    "of", "из", "da", "да", "de", "де", "des", "дез",
    "del", "дель", "di", "ди", "dos", "душ", "дос", "du", "дю",
    "la", "ла", "ля", "le", "ле", "haut", "от", "the",
)

/**
 * A comma-separated list of authors, reduced to initials.
 */
val String.initials get(): String {
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
    return this
        .replace(Reg.quotedSubstrings, " ")
        .replace("\"", " ")
        .split(",")
        .filter { author -> author.replace(".", "").replace("-", "").trim().isNotEmpty() }
        .joinToString(",") { author ->
            author
                .split("-")
                .filter { barrel -> barrel.replace(".", "").trim().isNotEmpty() }
                .joinToString("-") { barrel ->
                    barrel
                        .split(Reg.dots)
                        .filter { name -> name.isNotEmpty() }
                        .joinToString(".") { name -> formInitial(name) }
                } + "."
        }
}

/**
 * A human readable string representation
 * of a long integer, nicely rounded.
 */
val Long.humanBytes get(): String {
    val unitList = arrayOf(
        { q: Double -> q.trim(0) },
        { q: Double -> "${q.trim(0)}kB" },
        { q: Double -> "${q.trim(1)}MB" },
        { q: Double -> "${q.trim(2)}GB" },
        { q: Double -> "${q.trim(2)}TB" },
        { q: Double -> "${q.trim(2)}PB" },
    )
    if (this > 1) {
        val exponent = min(
            a = log(this.toDouble(), 1024.toDouble()).toInt(),
            b = unitList.size - 1
        )
        val quotient = this / 1024.toDouble().pow(exponent)
        return unitList[exponent](quotient)
    }
    if (this == 0L) return "0"
    if (this == 1L) return "1"
    throw RuntimeException("humanBytes error; bytes: $this")
}

/**
 * Evaluates the time interval between [start]
 * and the moment of call.
 * @receiver Clock.System
 * @return the time interval in milliseconds or seconds
 * as a formatted string.
 */
fun Clock.System.stop(start: Instant): String {
    val space = (this.now() - start).inWholeMilliseconds
    if (space < 3000L) return "${space}ms"
    return "${space.toDouble().div(1000).trim(1)}s"
}
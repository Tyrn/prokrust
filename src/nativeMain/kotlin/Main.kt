import com.github.ajalt.clikt.core.CliktCommand

val rDots = "[\\s.]+".toRegex()
val rQuotedSubstrings = """"(?:\\.|[^"\\])*"""".toRegex()

fun initials(authors: String): String {
    fun formInitial(name: String): String {
        if (name.isNotEmpty()) return name[0].toString().uppercase()
        return "*"
    }
    return authors
        .replace(rQuotedSubstrings, " ")
        .replace("\"", " ")
        .split(",")
        .filter { author -> author.replace(".", "").replace("-", "").trim().isNotEmpty() }
        .map { author ->
            author
                .split("-")
                .filter { barrel -> barrel.replace(".", "").trim().isNotEmpty() }
                .map { barrel ->
                    barrel
                        .split(rDots)
                        .filter { name -> name.isNotEmpty() }
                        .map { name -> formInitial(name) }
                        .joinToString(".")
                }
                .joinToString("-") + "."
        }
        .joinToString(",")
}

class Shoot : CliktCommand() {
    override fun run() {
        echo("¡Hola, Kitty!")
    }
}

fun main(args: Array<String>) = Shoot().main(args)
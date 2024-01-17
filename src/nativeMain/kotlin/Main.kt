import com.github.ajalt.clikt.core.CliktCommand

val rDots = """[\s.]+""".toRegex()
val rQuotedSubstrings = """"(?:\\.|[^"\\])*"""".toRegex()

fun initials(names: String): String {
    fun formInitial(name: String): String {
        if (name.isNotEmpty()) return name[0].toString().uppercase()
        return ""
    }
    return names
}

class Shoot : CliktCommand() {
    override fun run() {
        echo("¡Hola, Kitty!")
    }
}

fun main(args: Array<String>) = Shoot().main(args)
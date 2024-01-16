import com.github.ajalt.clikt.core.CliktCommand

fun initials(names: String): String {
   return names
}
class Shoot: CliktCommand() {
    override fun run() {
        echo("¡Hola, Kitty!")
    }
}
fun main(args: Array<String>) = Shoot().main(args)
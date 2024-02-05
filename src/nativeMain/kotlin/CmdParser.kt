import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int

object Icon {
    const val invalid = '❌'
    const val warning = "💧"
    const val bdelim = "🔹"
    const val odelim = "🔸"
    const val rsusp = '❓'
    const val suspicious = '❔'
    const val done = "🟢"
    const val column = '✔'
    const val link = '⚡'
    const val start = "💣"
    const val stop = "💥"
    const val use = '⭐'
}

class Prokrust : CliktCommand(
    help =
    """
    Prokrust a.k.a. Damastes is a CLI utility for copying directories and subdirectories
    containing supported audio files in sequence, naturally sorted.
    The end result is a "flattened" copy of the source subtree. "Flattened" means
    that only a namesake of the root source directory is created, where all the files get
    copied to, names prefixed with a serial number. Tag "Track Number"
    is set, tags "Title", "Artist", and "Album" can be replaced optionally.
    The writing process is strictly sequential: either starting with the number one file,
    or in the reverse order. This can be important for some mobile devices.
    ${Icon.invalid} Broken media;
    ${Icon.suspicious} Suspicious media;
    ${Icon.use} Really useful options.

    Examples; <src> as a directory:

    robinson-crusoe $ prokrust -va 'Daniel "Goldeneye" Defoe' -m 'Robinson Crusoe' .
    /run/media/player

    <src> as a single file:

    library $ prokrust -va 'Vladimir Nabokov' -u 'Ada' ada.ogg .
    """,
) {
    val verbose by option(
        "-v", "--verbose", help = "${Icon.use} Verbose output",
    ).flag()
    val dropTracknumber by option(
        "-d",
        "--drop-tracknumber",
        help = "Do not set track numbers",
    ).flag()
    val stripDecorations by option(
        "-s",
        "--strip-decorations",
        help = "Strip file and directory name decorations",
    ).flag()
    val fileTitle by option(
        "-f", "--file-title", help = "Use file name for title tag",
    ).flag()
    val fileTitleNum by option(
        "-F",
        "--file-title-num",
        help = "Use numbered file name for title tag",
    ).flag()
    val sortLex by option(
        "-x", "--sort-lex", help = "Sort files lexicographically",
    ).flag()
    val treeDst by option(
        "-t",
        "--tree-dst",
        help = "Retain the tree structure of the source album at destination",
    ).flag()
    val dropDst by option(
        "-p", "--drop-dst", help = "Do not create destination directory",
    ).flag()
    val reverse by option(
        "-r",
        "--reverse",
        help = "Copy files in reverse order (number one file is the last to be copied)",
    ).flag()
    val overwrite by option(
        "-w",
        "--overwrite",
        help = "Silently remove existing destination directory (not recommended)",
    ).flag()
    val dryRun by option(
        "-y",
        "--dry-run",
        help = "Without actually modifying anything (trumps -w, too)",
    ).flag()
    val count by option("-c", "--count", help = "Just count the files").flag()
    val prependSubdirName by option(
        "-i",
        "--prepend-subdir-name",
        help = "Prepend current subdirectory name to a file name",
    ).flag()
    val fileType by option(
        "-e",
        "--file-type",
        help = "Accept only specified audio files (e.g. -e flac, or even -e '*64kb.mp3')",
    )
    val unifiedName by option(
        "-u",
        "--unified-name",
        help = "${Icon.use} <text> for everything unspecified",
    )
    val artist by option(
        "-a",
        "--artist",
        help = "${Icon.use} Artist tag",
    )
    val album by option(
        "-m",
        "--album",
        help = "${Icon.use} Album tag",
    )
    val albumNum by option(
        "-b",
        "--album-num",
        help = "0..99; prepend <int> to the destination root directory name",
    ).int()
    val src by argument("src").validate { require(it.isNotBlank()) { "Source file cannot be blank." } }
    val dst by argument("dst").validate { require(it.isNotBlank()) { "Destination cannot be blank." } }

    override fun run() {
        appMain()
    }
}

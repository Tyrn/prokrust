dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("clikt", "4.2.2")
            library("clikt-clikt", "com.github.ajalt.clikt", "clikt").versionRef("clikt")
            plugin("kotlin-multiplatform", "org.jetbrains.kotlin.multiplatform").version("1.9.22")
        }
    }
}
rootProject.name = "prokrust"


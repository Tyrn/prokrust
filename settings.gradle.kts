dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("clikt", "4.2.2")
            library("clikt-clikt", "com.github.ajalt.clikt", "clikt").versionRef("clikt")
        }
    }
}
rootProject.name = "prokrust"


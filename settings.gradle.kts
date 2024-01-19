dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("clikt", "4.2.2")
            version("kotlin", "1.9.22")
            plugin(
                "multiplatform",
                "org.jetbrains.kotlin.multiplatform"
            ).versionRef("kotlin")
            library("clikt-clikt", "com.github.ajalt.clikt", "clikt").versionRef("clikt")
            library(
                "test-common",
                "org.jetbrains.kotlin",
                "kotlin-test-common"
            ).versionRef("kotlin")
            library(
                "test-annotations-common",
                "org.jetbrains.kotlin",
                "kotlin-test-annotations-common"
            ).versionRef("kotlin")
        }
    }
}
rootProject.name = "prokrust"


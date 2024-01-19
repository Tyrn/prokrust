dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("clikt", "4.2.2")
            version("kotlin", "1.9.22")
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
            plugin(
                "kotlin-multiplatform",
                "org.jetbrains.kotlin.multiplatform"
            ).versionRef("kotlin")
        }
    }
}
rootProject.name = "prokrust"


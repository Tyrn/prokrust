dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.22")
            version("clikt", "4.2.2")
            version("okio", "3.7.0")
            version("kotlinx-datetime", "0.5.0")
            version("kotter", "1.1.1")
            version("kotest", "5.7.2")
            plugin(
                "kotlin-multiplatform",
                "org.jetbrains.kotlin.multiplatform"
            ).versionRef("kotlin")
            plugin(
                "kotest-multiplatform",
                "io.kotest.multiplatform"
            ).versionRef("kotest")
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
            library(
                "okio-okio",
                "com.squareup.okio",
                "okio"
            ).versionRef("okio")
            library(
                "datetime",
                "org.jetbrains.kotlinx",
                "kotlinx-datetime"
            ).versionRef("kotlinx-datetime")
            library(
                "kotter",
                "com.varabyte.kotter",
                "kotter"
            ).versionRef("kotter")
            // library(
            //     "kotest-",
            //     "io.kotest",
            //     "kotest-"
            // ).versionRef("kotest")
            library(
                "kotest-property",
                "io.kotest",
                "kotest-property"
            ).versionRef("kotest")
            library(
                "kotest-assertions-core",
                "io.kotest",
                "kotest-assertions-core"
            ).versionRef("kotest")
            library(
                "kotest-framework-engine",
                "io.kotest",
                "kotest-framework-engine"
            ).versionRef("kotest")
            library(
                "kotest-framework-datatest",
                "io.kotest",
                "kotest-framework-datatest"
            ).versionRef("kotest")
            library(
                "kotest-runner-junit5",
                "io.kotest",
                "kotest-runner-junit5"
            ).versionRef("kotest")
        }
    }
}
rootProject.name = "prokrust"


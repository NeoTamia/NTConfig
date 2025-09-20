plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "NTConfig"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":core")
project(":core").name = "${rootProject.name}-core"

file("modules").listFiles()?.forEach { file ->
    if (file.isDirectory and !file.name.equals("build")) {
        println("Include modules:${file.name}")
        include(":modules:${file.name}")
        project(":modules:${file.name}").name = "${rootProject.name}-${file.name}"
    }
}

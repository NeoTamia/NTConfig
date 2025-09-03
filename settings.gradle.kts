plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "NTConfig"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":core")

file("modules").listFiles()?.forEach { file ->
    if (file.isDirectory and !file.name.equals("build")) {
        println("Include modules:${file.name}")
        include(":modules:${file.name}")
    }
}

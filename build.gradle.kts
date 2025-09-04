import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.net.URI

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "2.1.20"
    id("maven-publish")
}

group = "re.neotamia.config"
// x-release-please-start-version
version = "1.0.0-SNAPSHOT"
// x-release-please-end

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    group = "re.neotamia.config"
    version = rootProject.version

    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:26.0.2")

        testImplementation(kotlin("test"))
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    tasks.shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveAppendix.set(if (project.path == ":") "" else project.name)
    }

    tasks.test {
        useJUnitPlatform()
    }

    kotlin {
        jvmToolchain(21)
    }

    publishing {
        repositories {
            mavenLocal()
        }
    }
}

dependencies {
    implementation(projects.ntConfigCore)
    implementation(projects.modules.ntConfigJson)
    implementation(projects.modules.ntConfigToml)
    implementation(projects.modules.ntConfigYaml)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("com.charleskorn.kaml:kaml:0.94.0")
    implementation("com.akuleshov7:ktoml-core:0.7.1")
}

tasks.withType<KotlinJvmCompile>().configureEach {
    jvmTargetValidationMode.set(JvmTargetValidationMode.WARNING)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/NeoTamia/${project.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
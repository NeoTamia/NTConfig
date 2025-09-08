import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.net.URI

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
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

    group = "re.neotamia.config"
    version = rootProject.version

    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }
        maven {
            name = "neotamiaSnapshots"
            url = uri("https://repo.neotamia.re/snapshots")
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
    api(projects.ntConfigCore)
    api(projects.modules.ntConfigJson)
    api(projects.modules.ntConfigToml)
    api(projects.modules.ntConfigYaml)

    implementation(libs.bundles.nightConfigModule)
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

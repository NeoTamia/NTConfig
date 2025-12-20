import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.internal.extensions.stdlib.capitalized

plugins {
    kotlin("jvm")
    `maven-publish`
    `java-library`
    id("com.gradleup.shadow")
}

group = "re.neotamia.config"
// x-release-please-start-version
version = "0.0.1"
// x-release-please-end

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "neotamiaReleases"
        url = uri("https://repo.neotamia.re/releases")
    }
    maven {
        name = "neotamiaSnapshots"
        url = uri("https://repo.neotamia.re/snapshots")
    }
    maven {
        name = "neotamiaPrivate"
        url = uri("https://repo.neotamia.re/private")
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")

    testImplementation(kotlin("test"))
}

java {
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
}

val copyJars = tasks.register<Copy>("copyJars") {
    group = "publishing"
    description = "Copies the built JAR to a local directory."
    from(tasks.shadowJar)
    enabled = false
}

project.afterEvaluate {
    if (project.extra.has("localJarRepo")) {
        copyJars.configure {
            into(rootProject.layout.buildDirectory.dir("repo"))
            enabled = true
        }
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
    finalizedBy(copyJars)
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("stripped")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
    }
}

publishing {
    repositories {
        mavenLocal()
        maven {
            var repository = System.getProperty("repository.name", "snapshots")
            name = "neotamia${repository.capitalized()}"
            url = uri("https://repo.neotamia.re/${repository}")
            credentials(PasswordCredentials::class) {
                username = (findProperty("${name}Username") ?: System.getenv("MAVEN_USERNAME")) as String?
                password = (findProperty("${name}Password") ?: System.getenv("MAVEN_PASSWORD")) as String?
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                name = "NTConfig ${project.name}"
                description = "NTConfig, ${project.name} module."
                url = "https://github.com/NeoTamia/NTConfig"
                developers {
                    developer {
                        id = "NeoTamia"
                        url = "https://github.com/NeoTamia"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/NeoTamia/NTConfig.git"
                    developerConnection = "scm:git:ssh://git@github.com:NeoTamia/NTConfig.git"
                    url = "https://github.com/NeoTamia/NTConfig"
                }
            }
            // javadoc & sources jars already added with `components["java"]`
            from(components["java"])
        }
    }
}

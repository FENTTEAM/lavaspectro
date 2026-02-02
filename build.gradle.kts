import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "2.1.0"
    id("dev.arbjerg.lavalink.gradle-plugin") version "1.1.2"
    `maven-publish`
}

group = "team.fent.lavalink.lavaspectro"
val baseVersion = "1.0.0"

version = if (project.hasProperty("snapshot")) {
    try {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .start()
        val hash = process.inputStream.bufferedReader().readText().trim()
        if (process.waitFor() == 0) {
            "$baseVersion-$hash"
        } else {
            "$baseVersion-snapshot"
        }
    } catch (e: Exception) {
        "$baseVersion-snapshot"
    }
} else {
    baseVersion
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.lavalink.dev/releases") }
    maven { url = uri("https://maven.lavalink.dev/snapshots") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("dev.arbjerg.lavalink:plugin-api:4.1.2")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

kotlin {
    jvmToolchain(21)
}

lavalinkPlugin {
    name.set("lavaspectro")
    apiVersion.set("4")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/FENTTEAM/lavaspectro")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks.register("printVersion") {
    doLast {
        println(version)
    }
}
import java.io.File
import java.util.concurrent.TimeUnit

plugins {
    kotlin("jvm") version "2.1.0"
    id("dev.arbjerg.lavalink.gradle-plugin") version "1.1.2"
    `maven-publish`
}

group = "team.firefly.lavalink.lavaspectro"
val baseVersion = "1.0.0"

val githubRef = System.getenv("GITHUB_REF") ?: ""
val isRelease = githubRef.startsWith("refs/tags/") || project.hasProperty("release")
val isSnapshot = !isRelease

fun gitSha7(): String {
    val envSha = System.getenv("GITHUB_SHA")?.trim().orEmpty()
    if (envSha.isNotBlank()) return envSha.take(7)

    return runCatching {
        val proc = ProcessBuilder("git", "rev-parse", "--short=7", "HEAD")
            .directory(rootDir)
            .redirectErrorStream(true)
            .start()

        proc.waitFor(3, TimeUnit.SECONDS)
        proc.inputStream.bufferedReader().readText().trim().ifBlank { "unknown" }
    }.getOrDefault("unknown")
}

val commitHash = gitSha7()

val snapshotVersion = "$baseVersion-SNAPSHOT"
val commitVersion = "$baseVersion-$commitHash"

version = if (isSnapshot) snapshotVersion else baseVersion

repositories {
    mavenCentral()
    maven { url = uri("https://maven.lavalink.dev/releases") }
    maven { url = uri("https://maven.lavalink.dev/snapshots") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly("dev.arbjerg.lavalink:plugin-api:4.1.2")
    implementation("org.apache.commons:commons-math3:3.6.1")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
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
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }

        if (isSnapshot) {
            create<MavenPublication>("commit") {
                from(components["java"])
                version = commitVersion
            }
        }
    }

    repositories {
        val repoUrl = System.getenv("MAVEN_REPOSITORY")?.trim().orEmpty()
        val user = System.getenv("MAVEN_USERNAME")?.trim().orEmpty()
        val token = System.getenv("MAVEN_TOKEN")?.trim().orEmpty()

        if (repoUrl.isNotBlank()) {
            maven {
                name = "Remote"
                url = uri(repoUrl)
                if (user.isNotBlank() || token.isNotBlank()) {
                    credentials {
                        username = user
                        password = token
                    }
                }
            }
        }
    }
}

tasks.register("printVersion") {
    doLast { println(project.version.toString()) }
}

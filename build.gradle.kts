import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "2.1.0"
    id("dev.arbjerg.lavalink.gradle-plugin") version "1.1.2"
    `maven-publish`
}

group = "team.firefly.lavalink.lavaspectro"
val baseVersion = "1.0.0"

val isSnapshot = project.hasProperty("snapshot") || 
    System.getenv("GITHUB_REF_NAME") == "snapshot" || 
    System.getenv("GIT_TAG") == "snapshot" ||
    System.getenv("VERSION") == "snapshot"

version = if (isSnapshot) {
    "snapshot"
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
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/fireflyteam/lavaspectro")
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
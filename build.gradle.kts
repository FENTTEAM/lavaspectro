import org.gradle.api.publish.maven.MavenPublication
import org.gradle.authentication.http.BasicAuthentication

plugins {
    kotlin("jvm") version "2.1.0"
    id("dev.arbjerg.lavalink.gradle-plugin") version "1.1.2"
    `maven-publish`
}

group = "team.firefly.lavalink.lavaspectro"
val baseVersion = "1.0.0"

val githubRef = System.getenv("GITHUB_REF").orEmpty()
val isRelease = githubRef.startsWith("refs/tags/")
val isSnapshot = !isRelease

val commitHash7: String =
    (System.getenv("GITHUB_SHA")?.take(7))
        ?: runCatching {
            providers.exec { commandLine("git", "rev-parse", "--short=7", "HEAD") }
                .standardOutput.asText.get().trim()
        }.getOrDefault("unknown")

val snapshotVersion = "$baseVersion-SNAPSHOT"
val commitVersion = "$baseVersion-$commitHash7"

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

kotlin { jvmToolchain(21) }

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
            name = "fireflyteam"
            url = uri(System.getenv("MAVEN_REPOSITORY"))

            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_TOKEN")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        withType<MavenPublication>().configureEach {
            groupId = project.group.toString()
            artifactId = "lavaspectro"
        }

        if (isSnapshot) {
            create<MavenPublication>("commit") {
                groupId = project.group.toString()
                artifactId = "lavaspectro"
                version = commitVersion
                from(components["java"])
            }
        }
    }
}

tasks.register("printVersion") {
    doLast { println(project.version.toString()) }
}

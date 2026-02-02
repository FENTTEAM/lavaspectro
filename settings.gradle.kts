pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.lavalink.dev/releases") }
        maven { url = uri("https://maven.lavalink.dev/snapshots") }
    }
}

rootProject.name = "lavalink-spectrogram"
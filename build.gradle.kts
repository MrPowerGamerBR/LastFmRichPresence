import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "com.mrpowergamerbr.lastfmrichpresence"
version = "1.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.github.jagrosh:DiscordIPC:18b6096d71")
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha14")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("com.mrpowergamerbr.lastfmrichpresence.LastFmRichPresence")
}
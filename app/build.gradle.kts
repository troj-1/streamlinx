import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":providers"))
    implementation(project(":retrofit-jsoup-converter"))

    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")

    // Image loading for Compose Desktop
    implementation("media.kamel:kamel-image:0.9.3")
    implementation("io.ktor:ktor-client-okhttp:2.3.8")

    // Database
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("org.jetbrains.exposed:exposed-core:0.48.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.48.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.48.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.48.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("ch.qos.logback:logback-classic:1.5.3")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // Networking (for image loading)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Embedded video player (VLCJ)
    implementation("uk.co.caprica:vlcj:4.8.3")

    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "com.streamflixreborn.streamflix.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Msi, TargetFormat.Exe)
            packageName = "streamlinx"
            packageVersion = "0.0.1"
            description = "Streamlinx - Desktop & Linux Streaming Application"
            modules("java.naming", "java.sql", "java.management", "java.instrument", "java.desktop", "jdk.unsupported", "java.security.jgss", "java.net.http")

            linux {
                debPackageVersion = "0.0.1"
                appCategory = "video"
                dependsOn("vlc", "libvlc-dev", "libvlc5")
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":retrofit-jsoup-converter"))

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")

    // HTML Parsing
    implementation("org.jsoup:jsoup:1.19.1")

    // JavaScript Engine
    implementation("org.mozilla:rhino:1.8.0")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // WebSocket
    implementation("org.java-websocket:Java-WebSocket:1.5.3")
}

kotlin {
    jvmToolchain(17)
}

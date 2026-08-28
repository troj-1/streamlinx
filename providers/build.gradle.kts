plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")
    implementation("xmlpull:xmlpull:1.1.3.1")
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
    implementation("org.json:json:20240303")

    // WebSocket
    implementation("org.java-websocket:Java-WebSocket:1.5.3")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("com.google.code.gson:gson:2.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}

kotlin {
    jvmToolchain(17)
}

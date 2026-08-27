plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("org.jsoup:jsoup:1.19.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

kotlin {
    jvmToolchain(17)
}

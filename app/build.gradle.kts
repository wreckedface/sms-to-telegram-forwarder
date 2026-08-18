plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.smstelegram"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smstelegram"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "4.0"
    }
}
dependencies {
    implementation("androidx.work:work-runtime-ktx:2.10.1")
}

// Les plugins sont placés sur le même classpath (racine) pour que le plugin
// Kotlin voie le plugin Android. Le plugin Android n'est chargé que si le SDK
// Android est présent (voir settings.gradle.kts).
buildscript {
    val kotlinVersion = "2.1.21"
    val agpVersion = "8.7.3"
    val hasAndroidSdk = System.getenv("ANDROID_HOME") != null ||
        System.getenv("ANDROID_SDK_ROOT") != null ||
        file("local.properties").exists()

    repositories {
        if (hasAndroidSdk) {
            google()
        }
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:$kotlinVersion")
        if (hasAndroidSdk) {
            classpath("com.android.tools.build:gradle:$agpVersion")
        }
    }
}

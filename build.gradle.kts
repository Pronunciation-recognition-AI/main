plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://chaquo.com/maven/") }  // Chaquopy 저장소
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.1")  // Android Gradle Plugin 버전 명시
    }
}
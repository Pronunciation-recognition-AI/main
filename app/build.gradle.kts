plugins {
//    id("com.android.application")
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("com.chaquo.python")  // Chaquopy 플러그인을 모듈에 적용
}

android {
    namespace = "com.example.hackathon_project"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hackathon_project"

        // 최소 SDK 버전 설정
        minSdk = 26
        targetSdk = 34

        // NDK 설정을 추가하여 지원할 ABI 목록을 지정
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }


        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

chaquopy {
    defaultConfig {
        buildPython("C:/Users/eogks/AppData/Local/Programs/Python/Python310/python.exe")  // 정확한 Python 3.8 경로를 지정
        // buildPython("/usr/local/bin/python3.11")

        pip{
            install("numpy")
            install("scikit-learn")
            install("librosa==0.8.1")
            install("resampy==0.2.2")
            install("joblib")
        }
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth.ktx)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth") // Firebase Authentication
    implementation ("com.google.firebase:firebase-database-ktx:21.0.0") // Firebase Realtime Database
    implementation ("com.google.firebase:firebase-storage-ktx:21.0.0") // Firebase Storage 최신 버전
    // Picasso 의존성 추가
    implementation ("com.squareup.picasso:picasso:2.71828")
}
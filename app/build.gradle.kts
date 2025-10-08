import java.util.Properties
import java.io.*

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.cwec.n0w"
    compileSdk = 36


    defaultConfig {
        applicationId = "com.cwec.n0w"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    signingConfigs {
        create("release") {
            storeFile = if (!System.getenv("SIGNING_STORE_FILE").isNullOrEmpty()) {
                file(System.getenv("SIGNING_STORE_FILE"))
            } else {
                file(localProperties["SIGNING_STORE_FILE"]?.toString() ?: error("Signing config not found"))
            }
            storePassword = System.getenv("SIGNING_STORE_PASSWORD") ?: localProperties["SIGNING_STORE_PASSWORD"] as String? ?: error("Store password not found")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS") ?: localProperties["SIGNING_KEY_ALIAS"] as String? ?: error("Key alias not found")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD") ?: localProperties["SIGNING_KEY_PASSWORD"] as String? ?: error("Key password not found")

            enableV1Signing = false
            enableV2Signing = true
            enableV3Signing = false
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material)
    implementation(libs.androidx.compose.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.fxratetracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fxratetracker"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Yeah, yeah, this should not be stored in APK altogether.
        // As just by decompiling apk and running string on it will show plain api key.
        // We can obfuscate it, like storing string bytes and then recreating it,
        // but it is a futile attempt, as anyone can deobfuscate it. Its just an extra step
        //
        // Ideally api that requires api key usage should be proxied.
        val properties = gradleLocalProperties(rootDir, providers)
        val apiKey = properties.getProperty("api.key")
        require(!apiKey.isNullOrBlank()) {
            "Api key was not provided. Update your root local.properies with non-empty api.key"
        }
        buildConfigField("String", "API_KEY", "\"$apiKey\"")

        val fxRateListRefreshPeriod = properties.getProperty("fxrate.refresh.period").toIntOrNull()
        require(fxRateListRefreshPeriod != null && fxRateListRefreshPeriod > 0) {
            "Valid fx rate refresh period was not provided. Update your root local.properies with valid positive integer value"
        }
        buildConfigField("int", "FX_RATE_REFRESH_PERIOD_SECONDS", "$fxRateListRefreshPeriod")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.datastore)
    implementation(libs.slack.circuit)
    implementation(libs.slack.circuitx.gesture.navigation)
    implementation(libs.squareup.okhttp.logging)
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.converter.ktx.serialization)
    implementation(libs.ktx.serialization.json)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
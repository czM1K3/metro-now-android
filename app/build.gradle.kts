plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.apollo.client)
    alias(libs.plugins.ksp)
}

apollo {
    service("service") {
        packageName.set("dev.metronow.android")
    }
}

android {
    namespace = "dev.metronow.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.metronow.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    flavorDimensions += "locationBackend"
    productFlavors {
        create("gms") {
            dimension = "locationBackend"
        }
        create("vanilla") {
            versionNameSuffix = "-nogms"
            dimension = "locationBackend"
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        jvmToolchain(8)
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.apollo.runtime)
    implementation(libs.android.navigation.compose)
    implementation(libs.kotlin.serialization.json)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.android.compose)
    implementation(libs.datastore.preferences)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.android.room)
    ksp(libs.android.room.compiler)
    "gmsImplementation"(libs.gms.play.services.location)
    implementation(libs.accompanist.permissions)
    implementation(libs.coil.kt.coil.compose)
}
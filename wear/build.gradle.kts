plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.metronow.android.wear"
    compileSdk = 37

    defaultConfig {
        // Same Play listing as the phone app (Wear OS form factor track)
        applicationId = "dev.metronow.android"
        minSdk = 30
        targetSdk = 36
        // Never collide with the phone's version codes
        versionCode = 1_000_000 + libs.versions.appVersionCode.get().toInt()
        versionName = libs.versions.appVersionName.get()

        // :shared has gms/vanilla flavors; the watch is gms only
        missingDimensionStrategy("locationBackend", "gms")
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

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.navigation)
    implementation(libs.wear.compose.ui.tooling)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android.compose)
    implementation(libs.accompanist.permissions)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.apollo.client)
    alias(libs.plugins.ksp)
}

apollo {
    service("service") {
        packageName.set("dev.metronow.android")
    }
}

android {
    namespace = "dev.metronow.android.shared"
    compileSdk = 37

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    flavorDimensions += "locationBackend"
    productFlavors {
        create("gms") {
            dimension = "locationBackend"
        }
        create("vanilla") {
            dimension = "locationBackend"
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
}

dependencies {
    // Part of the public API: Koin modules, StopsViewModel, Compose Color mappings
    api(platform(libs.koin.bom))
    api(libs.koin.android)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.ui.graphics)

    implementation(libs.androidx.core.ktx)
    implementation(libs.apollo.runtime)
    implementation(libs.datastore.preferences)
    implementation(libs.android.room)
    ksp(libs.android.room.compiler)
    "gmsImplementation"(libs.gms.play.services.location)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

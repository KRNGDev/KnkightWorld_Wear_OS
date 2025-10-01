
plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.knightworld.wear"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.knightworld.wear"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.wear:wear:1.3.0")
    // Watch face libraries 1.2.0 are the latest stable artifacts available from Google Maven.
    val watchfaceVersion = "1.2.0"
    implementation("androidx.wear.watchface:watchface:$watchfaceVersion")
    implementation("androidx.wear.watchface:watchface-style:$watchfaceVersion")
    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:$watchfaceVersion")
}

import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "sabuj.m.truedistance"
    compileSdk = 35

    defaultConfig {
        applicationId = "sabuj.m.truedistance"
        minSdk = 29
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY", "")
        buildConfigField("String", "MAPS_API_KEY", "\"${localProperties.getProperty("MAPS_API_KEY", "")}\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }

    compileOptions {
        sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
        targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // ConstraintLayout — required for Flow helper used in Speedometer layouts
    // (project-overview.md §12.4 / res/layout/fragment_speedometer.xml)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // RecyclerView + CardView — Saved Locations (§6.1.2), History (§6.1.3), Past Trips (§6.2.2)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Fragment
    implementation("androidx.fragment:fragment-ktx:1.8.2")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Lifecycle / ViewModel / StateFlow
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    // Needed for foreground service + coroutine scope (background tracking, §6.1.4/§6.2)
    implementation("androidx.lifecycle:lifecycle-service:2.8.4")

    // Hilt (compiler/runtime versions aligned — was mismatched 2.51.1/2.52)
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-android-compiler:2.52")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Kotlinx Serialization — used by LatLngListConverter (Trip.pathPoints JSON, §8)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // Google Maps & Places & Location
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.libraries.places:places:3.5.0")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Data Store
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Test Dependancies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Transition
    implementation("androidx.transition:transition:1.5.1")

}
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.signconnect_app"
    compileSdk = 36 // Updated to 36 to support newer dependencies

    defaultConfig {
        applicationId = "com.example.signconnect_app"
        minSdk = 28
        targetSdk = 36 // Updated to match compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // --- CameraX Dependencies ---
    val cameraxVersion = "1.3.3"
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // --- MediaPipe Framework ---
    implementation("com.google.mediapipe:tasks-vision:0.10.14")

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
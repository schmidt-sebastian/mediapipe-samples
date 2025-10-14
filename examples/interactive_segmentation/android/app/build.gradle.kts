plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("de.undercouch.download")
    id("com.google.mediapipe.tasks.vision.provider.VisionProviderPlugin")
}

android {
    namespace = "com.mediapipe.example.interactivesegmentation"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mediapipe.example.interactivesegmentation"
        minSdk = 33
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    aaptOptions {
        noCompress.add("tflite")
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        viewBinding = true
    }
    sourceSets {
        getByName("main") {
            java.srcDir("build/generated/source/buildInfo/kotlin")
            // Add the generated Kotlin source directory here
            kotlin.srcDir(layout.buildDirectory.dir("generated/source/main/kotlin"))
        }
    }
}


// Define the asset directory as an extra property
extra["ASSET_DIR"] = "$projectDir/src/main/assets"

// Apply the model downloader script
apply(from = "download_model.gradle")

dependencies {
    implementation(project(":shared"))
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // MediaPipe Library
    implementation("com.google.mediapipe:tasks-vision:0.10.30")
    implementation("com.google.android.play:feature-delivery:2.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("com.google.truth:truth:1.1.3")
}
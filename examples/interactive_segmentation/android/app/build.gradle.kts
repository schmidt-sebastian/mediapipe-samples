import com.google.mediapipe.tasks.vision.provider.GenerateVisionProviderTask
import com.google.mediapipe.tasks.vision.provider.VisionModel

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("de.undercouch.download")
    id("com.google.mediapipe.tasks.vision.provider")
}

tasks.named<GenerateVisionProviderTask>("generateVisionProvider") {
    tasksConfiguration.get().apply {
        // Use the register method to configure each task
        register("FaceDetector") {
            defaultModel.set("BLAZE_FACE_SHORT_RANGE")
            models.set(listOf(
                VisionModel("BLAZE_FACE_SHORT_RANGE", "blaze_face_short_range", "1.0")
            ))
        }

        register("FaceLandmarker") {
            defaultModel.set("FACE_LANDMARKER")
            models.set(listOf(
                VisionModel("FACE_LANDMARKER", "face_landmarker", "1.0"),
                VisionModel("FACE_LANDMARKER_V2_WITH_BLENDSHAPES", "face_landmarker_v2_with_blendshapes", "2.0")
            ))
        }

        register("ImageClassifier") {
            defaultModel.set("EFFICIENTNET_LITE0")
            models.set(listOf(
                VisionModel("EFFICIENTNET_LITE0", "efficientnet_lite0", "1.0", "FLOAT32"),
                VisionModel("EFFICIENTNET_LITE2", "efficientnet_lite2", "1.0", "FLOAT32")
            ))
        }
    }
}

android {
    namespace = "com.mediapipe.example.interactivesegmentation"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mediapipe.example.interactivesegmentation"
        minSdk = 34
        targetSdk = 33
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
        }
    }
}

// Define the asset directory as an extra property
extra["ASSET_DIR"] = "$projectDir/src/main/assets"

// Apply the model downloader script
apply(from = "download_model.gradle")

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // MediaPipe Library
    implementation("com.google.mediapipe:tasks-vision:0.10.28")
    implementation("com.google.android.play:feature-delivery:2.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("com.google.truth:truth:1.1.3")
}
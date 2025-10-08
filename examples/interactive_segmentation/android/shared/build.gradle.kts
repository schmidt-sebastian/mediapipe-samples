plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.google.mediapipe.tasks.vision.provider"
    compileSdk = 36


    android {
        namespace = "com.google.mediapipe.tasks.vision.provider"
        compileSdk = 36

        defaultConfig {
            minSdk = 33
        }
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
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("com.google.mediapipe:tasks-vision:0.10.30")
    implementation ("com.google.android.play:asset-delivery:2.3.0")
    implementation ("com.google.android.play:ai-delivery:0.1.1-alpha01")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("com.google.android.play:feature-delivery-ktx:2.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
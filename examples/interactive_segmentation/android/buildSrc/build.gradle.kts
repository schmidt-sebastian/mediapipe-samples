import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
//    implementation("androidx.appcompat:appcompat:1.7.1") // Or a newer version
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4") // Or a newer version
    implementation("com.android.tools.build:gradle:8.12.3")
    implementation("com.squareup:javapoet:1.13.0")
//    implementation("com.google.android.play:feature-delivery:2.1.0")
//    implementation("com.google.mediapipe:tasks-core:0.10.29")
}

// Add this block to register the plugin
gradlePlugin {
    plugins {
        register("mediapipe-npu") {
            id = "com.google.mediapipe.npu"
            implementationClass = "com.google.mediapipe.npu.NpuProvider"
        }
    }
}
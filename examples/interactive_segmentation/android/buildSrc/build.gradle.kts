import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.12.3")
    implementation("com.palantir.javapoet:javapoet:0.7.0")
}

// Add this block to register the plugin
gradlePlugin {
    plugins {
        register("visionProvider") {
            id = "com.google.mediapipe.tasks.vision.provider"
            implementationClass = "com.google.mediapipe.tasks.vision.provider.VisionProviderPlugin"
        }
    }
}
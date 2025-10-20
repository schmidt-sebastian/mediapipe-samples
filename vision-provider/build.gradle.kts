import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `maven-publish`
}


dependencies {
    implementation("com.android.tools.build:gradle:8.12.3")
    implementation("com.squareup:kotlinpoet:2.2.0")
    implementation("com.google.code.gson:gson:2.13.2")
}

group = "com.google.mediapipe.tasks.vision.provider"
version = "1.0.241"


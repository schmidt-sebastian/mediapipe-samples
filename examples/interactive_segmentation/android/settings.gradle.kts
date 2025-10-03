// 1. pluginManagement MUST be the first block.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id("com.android.application") version "8.12.3" apply false
        id("com.android.library") version "8.12.3" apply false
        id("org.jetbrains.kotlin.android") version "2.2.20" apply false
        id("de.undercouch.download") version "5.6.0" apply false
    }
}

// 2. The buildscript block comes AFTER pluginManagement.
buildscript {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        classpath("com.google.mediapipe.tasks.vision.provider:com.google.mediapipe.tasks.vision.provider.gradle.plugin:1.0.5")
    }
}

// 3. Now apply the plugin.
apply(plugin = "com.google.mediapipe.tasks.vision.provider.ModuleGeneratorPlugin")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}

rootProject.name = "Interactive Segmentation"
include(":app")
include(":shared")
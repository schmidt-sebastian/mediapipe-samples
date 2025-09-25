package com.google.mediapipe.npu

// In your main plugin class (e.g., NpuPackagerPlugin.kt)
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask
import java.io.File

// Define a task class
abstract class CreateNpuModuleTask : DefaultTask() {
    @get:Input
    var vendorName: String? = null

    @TaskAction
    fun createModule() {
        val vendor = vendorName ?: throw IllegalArgumentException("Vendor name must be provided")
        val moduleName = "npu_$vendor"
        val rootDir = project.rootDir
        val moduleDir = File(rootDir, moduleName)

        println("▶️ Creating module for vendor: $vendor at ${moduleDir.path}")

        // 1. Create directory structure
        if (moduleDir.exists()) {
            println("✅ Module directory already exists. Skipping.")
        } else {
            moduleDir.mkdirs()
            File(moduleDir, "src/main/java").mkdirs()
            // etc. for jniLibs, res...
        }

        // 2. Create build.gradle.kts for the module
        val buildFile = File(moduleDir, "build.gradle.kts")
        if (!buildFile.exists()) {
            buildFile.writeText("""
                plugins {
                    id("com.android.dynamic-feature")
                    id("org.jetbrains.kotlin.android")
                }
                
                android {
                    namespace = "com.example.app.$moduleName"
                    // ... other boilerplate android config
                }
                
                dependencies {
                    implementation(project(":app"))
                }
            """.trimIndent())
        }

        // 3. Create AndroidManifest.xml
        // ... (similar file writing logic)

        // 4. Add module to settings.gradle.kts
        val settingsFile = File(rootDir, "settings.gradle.kts")
        val moduleInclude = "include(\":$moduleName\")"
        if (!settingsFile.readText().contains(moduleInclude)) {
            settingsFile.appendText("\n$moduleInclude\n")
            println("✅ Added '$moduleName' to settings.gradle.kts")
        }

        println("🎉 Done! Please sync your project in Android Studio.")
    }
}

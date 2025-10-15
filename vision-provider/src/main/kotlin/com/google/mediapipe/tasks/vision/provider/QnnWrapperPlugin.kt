package com.google.mediapipe.tasks.vision.provider

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

/**
 * A Gradle project plugin that configures an Android library to wrap the
 * Qualcomm QNN runtime. It applies dependencies, generates the device
 * targeting config, and extracts native libraries.
 */
class QnnWrapperPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // This plugin assumes the module is an Android library.
        project.plugins.apply("com.android.library")

        // Get the Android extension to configure it.
        val android = project.extensions.getByType(LibraryExtension::class.java)

        configureAndroidDefaults(project, android)
        createDeviceConfigGenerationTask(project)
        createSoExtractionTask(project)
    }

    /**
     * Configures the basic Android settings and adds the QNN runtime dependency.
     */
    private fun configureAndroidDefaults(project: Project, android: LibraryExtension) {
        android.apply {
            compileSdk = 34
            defaultConfig {
                minSdk = 33
            }
            // Add the Qualcomm QNN runtime dependency from Maven Central.
            project.dependencies.add("implementation", "com.qualcomm.qti:qnn-runtime:2.16.0.240129@aar")
        }
    }

    /**
     * Registers a task to generate the device_targeting_config.xml file.
     */
    private fun createDeviceConfigGenerationTask(project: Project) { // ktlint-disable-line no-unused-parameter
        project.tasks.register("generateDeviceTargetingConfig") {
            group = "build"
            description = "Generates the XML for device targeting."

            doLast {
                val xmlDir = project.file("src/main/res/xml")
                xmlDir.mkdirs() // Ensure the directory exists
            }
        }

        // Ensure this task runs before Android resources are processed.
        project.afterEvaluate {
            project.tasks.matching { it.name.startsWith("generate") && it.name.endsWith("Resources") }.all {
                dependsOn("generateDeviceTargetingConfig")
            }
        }
    }

    /**
     * Registers a Copy task to download the QNN AAR, unzip it, and copy the .so files
     * into the standard jniLibs directory.
     */
    private fun createSoExtractionTask(project: Project) {
        val extractTask = project.tasks.register("extractQnnSoFiles", Copy::class.java) {
            group = "build"
            description = "Extracts .so files from the Qualcomm QNN runtime AAR."

            val jniLibsDir = project.file("src/main/jniLibs")
            outputs.dir(jniLibsDir)

            doFirst {
                try {
                    // Create a detached configuration to resolve the QNN runtime AAR specifically for this task.
                    val qnnDependency = project.dependencies.create("com.qualcomm.qti:qnn-runtime:2.16.0.240129@aar")
                    val qnnConfiguration = project.configurations.detachedConfiguration(
                        qnnDependency
                    );
                    val aarFile = qnnConfiguration.singleFile

                    // Configure the copy task to extract from the zip.
                    from(project.zipTree(aarFile)) {
                        include("jni/**/*.so")
                    }
                    into(jniLibsDir)
                    // Remap path from 'jni/arm64-v8a/libQnnHtp.so' to 'arm64-v8a/libQnnHtp.so'
                    eachFile {
                        path = path.substringAfter("jni/")
                    }
                } catch (e: Exception) {
                    project.logger.warn("Could not resolve qnn-runtime AAR file to extract .so files. Error: ${e.message}")
                }
            }
        }

        // Hook the extraction task to run before the main preBuild task.
        project.tasks.named("preBuild").configure {
            dependsOn(extractTask)
        }
    }

}



package com.google.mediapipe.tasks.vision.provider

import com.google.gson.Gson
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.extra
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

/**
 * Handles the dynamic generation and inclusion of AI Pack modules.
 * This generator creates a separate, self-contained module for each model variant
 * and for each QNN NPU variant.
 */
class AIPackModuleGenerator {


    companion object {
        /**
         * Common libraries required by most/all QNN HTP/DSP variants.
         * Based on your request, this includes libQnnDsp.so. I've also
         * included Htp and System as they are likely required by the HTP variants.
         */
        val QNN_COMMON_FILES = listOf(
            "libQnnDsp.so",
            "libQnnHtp.so",
            "libQnnSystem.so"
        )

        /**
         * Defines the module name and its specific, unique .so files.
         * This map is used to generate one module per entry.
         */

        /**
         * Gets the Hexagon architecture version string (e.g., "v75") for a given SoC identifier.
         * This is updated based on the new ground truth SoC-to-Arch map.
         */
        fun getHexagonVersionForSoC(socIdentifier: String): String? {
            return when {
                // v79
                socIdentifier.contains("SM8750", ignoreCase = true) ||
                        socIdentifier.contains("SXR2330P", ignoreCase = true) -> "v79"

                // v75
                socIdentifier.contains("SM8650", ignoreCase = true) -> "v75"

                // v73
                socIdentifier.contains("SM8550", ignoreCase = true) ||
                        socIdentifier.contains("SSG2115P", ignoreCase = true) ||
                        socIdentifier.contains("SSG2125P", ignoreCase = true) ||
                        socIdentifier.contains("SXR1230P", ignoreCase = true) -> "v73"

                // v69
                socIdentifier.contains("SM8450", ignoreCase = true) ||
                        socIdentifier.contains("SM8475", ignoreCase = true) ||
                        socIdentifier.contains("SXR2230P", ignoreCase = true) -> "v69"

                // v68
                socIdentifier.contains("SA8295", ignoreCase = true) -> "v68"

                // Return null if the SoC is not in our list
                else -> null
            }
        }

        /**
         * Maps QNN architecture variants to their required library files.
         * Added the new v79 entry.
         */
        val QNN_VARIANT_MAP = mapOf(
            "qnn_v68" to listOf("libQnnHtpV68Skel.so", "libQnnHtpV68Stub.so"),
            "qnn_v69" to listOf("libQnnHtpV69Skel.so", "libQnnHtpV69Stub.so"),
            "qnn_v73" to listOf("libQnnHtpV73Skel.so", "libQnnHtpV73Stub.so"),
            "qnn_v75" to listOf("libQnnHtpV75Skel.so", "libQnnHtpV75Stub.so"),
            "qnn_v79" to listOf("libQnnHtpV79Skel.so", "libQnnHtpV79Stub.so"),
        )

        /**
         * Provides a reverse mapping from QNN architecture to a list of compatible SoCs.
         * This is now generated from the ground truth data.
         */
        val QNN_REVERSE_MAP = mapOf(
            "qnn_v79" to listOf("sm8750", "sxr2330p"),
            "qnn_v75" to listOf("sm8650"),
            "qnn_v73" to listOf("sm8550", "ssg2115p", "ssg2125p", "sxr1230p"),
            "qnn_v69" to listOf("sm8450", "sm8475", "sxr2230p"),
            "qnn_v68" to listOf("sa8295")
        )
    }

    /**
     * Loops through QNN_VARIANT_MAP and generates a separate module for each.
     */
    fun generateAndIncludeQnnModules(settings: Settings, parsedWrapper: TasksWrapper) {

// 1. Determine the delivery mode XML based on the configuration string
        val deliveryXml = when (parsedWrapper.npuModuleDelivery?.lowercase()) {
            "install-time" -> """
        <dist:install-time />
    """.trimIndent()

            "fast-follow" -> """
        <dist:fast-follow />
    """.trimIndent()

            "on-demand" -> """
        <dist:on-demand />
    """.trimIndent()

            else -> { // This also handles the null case
                // Default to on-demand if the value is invalid or null
                println("Warning: Invalid npuModuleDelivery value '${parsedWrapper.npuModuleDelivery}'. Defaulting to 'on-demand'.")
                """
        <dist:on-demand />
        """.trimIndent()
            }
        }

        val npuModulesDir = File(settings.rootDir, "build/generated/npu-modules")
        val supportedAbis = listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        for ((moduleName, socList) in QNN_REVERSE_MAP) {

            val moduleDir = File(npuModulesDir, moduleName)
            val mainSrcDir = File(moduleDir, "src/main")
            mainSrcDir.mkdirs()
            val jniLibsDir = File(mainSrcDir, "jniLibs")

            supportedAbis.forEach { abi ->
                val abiDir = File(jniLibsDir, abi)
                abiDir.mkdirs()
                // If this is NOT the arm64-v8a directory, create a .keep file
                // to force Gradle to package the empty directory.
                if (abi != "arm64-v8a") {
                    try {
                        File(abiDir, "dummy.so").createNewFile()
                    } catch (e: IOException) {
                        System.err.println("Failed to create .keep file for $abi: ${e.message}")
                    }
                }
            }


            // 2. Create the build.gradle.kts for the new module.
            val buildFile = File(moduleDir, "build.gradle.kts")
            // Each module needs a unique namespace
            val namespace = "com.google.mediapipe.examples.qnnwrapper.$moduleName"
            buildFile.writeText("""
        plugins {
            id("com.android.dynamic-feature")
            kotlin("android")
        }

        android {
            namespace = "$namespace"
            compileSdk = ${parsedWrapper.compileSdk}
            defaultConfig {
                minSdk = ${parsedWrapper.minSdk}
            }
        }

        dependencies {
            implementation(project(":app"))
        }
        """.trimIndent())

            // 3. Create the AndroidManifest.xml
            val manifestFile = File(mainSrcDir, "AndroidManifest.xml")

            // Generate the XML for device groups based on the socList
            val deviceGroupsXml = socList.joinToString(separator = "\n                          ") { soc ->
                // Assumes device group name format is "qualcomm_<soc_model_lowercase>"
                """<dist:device-group dist:name="qualcomm_${soc.lowercase()}"/>"""
            }

            // Each module gets a unique title string
            val titleString = "title_qnn_wrapper"
            manifestFile.writeText("""
        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:dist="http://schemas.android.com/apk/distribution">
            
            <dist:module dist:title="@string/title_qnn_wrapper">

                <dist:delivery>
                    $deliveryXml
                </dist:delivery>
                <dist:fusing dist:include="false" />
                <dist:conditions>
                   <dist:device-groups>
                      $deviceGroupsXml
                   </dist:device-groups>
                </dist:conditions>
            </dist:module>

        </manifest>
        """.trimIndent())

            // 4. Include the module in the build
            settings.include(":$moduleName")
            settings.project(":$moduleName").projectDir = moduleDir
            println("✅ Included QNN module: :$moduleName")
        }
    }

    /**
     * The main entry point called from settings.gradle.kts.
     */
    fun generateAndIncludeModules(settings: Settings) {

        // ... (rest of the function is identical to your original) ...
        val rootDir = settings.rootProject.projectDir
        val configFile = File(rootDir, "tasks.json")
        val aiPackModulesDir = File(rootDir, "build/generated/aipack-modules").apply { mkdirs() }
        aiPackModulesDir.mkdirs()

        if (!configFile.exists()) {
            System.err.println("Warning: Vision config file not found at ${configFile.path}")
            return
        }

        try {
            println("YES! Generating AI Pack modules from: ${configFile.name}")
            val jsonContent = configFile.readText()
            val gson = Gson()
            val parsedWrapper = gson.fromJson(jsonContent, TasksWrapper::class.java)
            generateModuleFiles(parsedWrapper.tasks, aiPackModulesDir)
            generateAndIncludeQnnModules(settings, parsedWrapper)
        } catch (e: IOException) {
            throw RuntimeException("Failed to generate AI Pack modules from ${configFile.path}", e)
        }

        // Generate all the new QNN modules
        aiPackModulesDir.listFiles { file -> file.isDirectory }?.forEach { moduleDir ->
            val moduleName = moduleDir.name
            settings.include(":$moduleName")
            settings.project(":$moduleName").projectDir = moduleDir
            println("✅ Included module: :$moduleName")
        }

        val resourcePath = "/device_targeting_configuration.xml"
        val destinationDir = File(settings.rootProject.projectDir, "app/build/generated/res/xml")
        val destinationFile = File(destinationDir, "device_targeting_configuration.xml")

        this::class.java.getResourceAsStream(resourcePath).use { inputStream ->
            if (inputStream == null) {
                System.err.println(
                    "Warning: Asset file not found in plugin resources at path: $resourcePath"
                )
                return
            }
            destinationDir.mkdirs()
            destinationFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            System.err.println("Copied plugin resource to ${destinationFile.path}")
        }
    }

    @Throws(IOException::class)
    private fun generateModuleFiles(
        tasks: Map<String, VisionTaskConfig>,
        aiPackModulesDir: File
    ) {
        for ((taskName, config) in tasks) {
            for (modelName in config.models) {
                val visionModel = VisionModel.fromCanonicalName(taskName, modelName)
                val modelUrls = visionModel.createModelUrls()
                val variants = visionModel.getVariants()
                val moduleDirName = "aipack_${modelName}"
                val moduleDir = File(aiPackModulesDir, moduleDirName)
                variants.forEachIndexed { i, variant ->
                    val modelUrl = modelUrls[i]
                    val assetsDir = File(moduleDir, "src/main/assets/model#group_" + visionModel.getVariants()[i]).apply { mkdirs() }
                    val modelFileName = visionModel.modelName()
                    val destinationFile = File(assetsDir, modelFileName + visionModel.task().modelExtension)
                    downloadFile(modelUrl, destinationFile)
                }

                val buildScriptPath = moduleDir.toPath().resolve("build.gradle.kts")
                val buildScriptContent = generateAIPackBuildScript(moduleDirName)
                Files.writeString(
                    buildScriptPath,
                    buildScriptContent,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            }
        }
    }

    private fun downloadFile(sourceUrl: String, destinationFile: File) {
        if (destinationFile.exists()) {
            return
        }
        println("⬇️ Downloading $sourceUrl...")
        try {
            val url = URL(sourceUrl)
            val connection = url.openConnection() as HttpURLConnection

            if (hfToken.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $hfToken")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.use { input ->
                    Files.copy(input, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            } else {
                System.err.println(
                    "❌ Failed to download $sourceUrl. " +
                            "Server responded with code: ${connection.responseCode}"
                )
            }
        } catch (e: IOException) {
            System.err.println("❌ Error downloading $sourceUrl: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun generateAIPackBuildScript(packName: String): String {
        return """
        plugins {
            id("com.android.ai-pack")
        }
        aiPack {
            packName = "$packName"
            dynamicDelivery {
                deliveryType = "install-time"
            }
        }
        """.trimIndent()
    }
}
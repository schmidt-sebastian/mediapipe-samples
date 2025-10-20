package com.google.mediapipe.tasks.vision.provider


import com.google.gson.Gson
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.nio.file.Files

/**
 * The main Gradle task that orchestrates the generation of the VisionProvider class
 * and the associated AI Pack modules.
 */
class GenerateVisionProviderTask(
    private val project: Project,
    private val tasksConfiguration: NamedDomainObjectContainer<VisionTask>
) {
    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        // NEW: Parse the JSON file to populate the tasksConfiguration
        parseConfigurationFromJson()

        // Delegate code generation to the helper class
        val codeGenerator = VisionProviderCodeGenerator(
            this.tasksConfiguration
        )
        val fileSpec = codeGenerator.generateVisionProviderFile()


        // Write the generated Kotlin file
        val outputDir = project.file(OUTPUT_DIR)
        outputDir.mkdirs()
        System.err.println("Output dir for generated code: " + outputDir)
        fileSpec.writeTo(outputDir)



    }

    // NEW: Helper method to parse JSON and configure tasks
    /**
     * Parses a JSON configuration file to configure and return a container of vision tasks.
     *
     * @return The configured NamedDomainObjectContainer for vision tasks.
     * @throws IOException if the configuration file cannot be read.
     */
    @Throws(IOException::class)
    private fun parseConfigurationFromJson(): NamedDomainObjectContainer<VisionTask> {
        // Get the container that will be configured and returned.
        val tasks = this.tasksConfiguration

        val configFile = project.rootDir.resolve(JSON_FILE)

        // If the config file doesn't exist, return the empty container.
        if (!configFile.exists()) {
            throw RuntimeException("Failed to find file at " + configFile.toPath())
        }

        val jsonContent = String(Files.readAllBytes(configFile.toPath()))

        val gson = Gson()
        val parsedWrapper = gson.fromJson(jsonContent, TasksWrapper::class.java)

        parsedWrapper.tasks.forEach { (name: String, config: VisionTaskConfig?) ->
            tasks.register(name) {
                if (config != null) {
                    defaultModel.set(config.defaultModel)
                    models.set(config.models)
                }
            }
        }
        return tasks
    }

    companion object {
        private const val OUTPUT_DIR = "build/generated/source/main/kotlin"
        private const val JSON_FILE = "tasks.json"
    }
}
package com.google.mediapipe.tasks.vision.provider

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

/**
 * Represents a single vision task configuration.
 */
abstract class VisionTask @Inject constructor(
    /**
     * Gets the name of the task (e.g., "FaceDetector").
     * This must match one of the names in the BaseTask enum, in PascalCase.
     * @return The task name.
     */
    @JvmField @get:Input val name: String
) {

    /**
     * Gets the type of the vision task, derived from the task name.
     * This property converts the PascalCase name (e.g., "FaceDetector")
     * to the corresponding UPPER_SNAKE_CASE enum constant (e.g., FACE_DETECTOR).
     *
     * @return The BaseTask enum constant for this task.
     * @throws IllegalArgumentException if the name does not correspond to a valid BaseTask.
     */
    @get:Input
    val type: Type by lazy {
        // Convert PascalCase name to UPPER_SNAKE_CASE for enum matching
        val enumName = name.replace(Regex("(?<=[a-z])(?=[A-Z])"), "_").uppercase()
        try {
            Type.valueOf(enumName)
        } catch (e: IllegalArgumentException) {
            // Provide a helpful error message if the mapping fails
            throw IllegalArgumentException(
                "Invalid task name '$name'. No matching BaseTask found for '$enumName'. " +
                        "Available tasks are: ${Type.values().joinToString { it.name }}"
            )
        }
    }

    /**
     * Defines the types of MediaPipe vision tasks and their corresponding model file extensions.
     */
    enum class Type(
        /**
         * The required model file extension for the vision task (e.g., ".task" or ".tflite").
         */
        val modelExtension: String
    ) {
        FACE_DETECTOR(".task"),
        FACE_LANDMARKER(".task"),
        GESTURE_RECOGNIZER(".task"),
        HAND_LANDMARKER(".task"),
        IMAGE_CLASSIFIER(".task"),
        IMAGE_EMBEDDER(".task"),
        IMAGE_SEGMENTER(".task"),
        INTERACTIVE_SEGMENTER(".task"),
        OBJECT_DETECTOR(".task"),
        POSE_LANDMARKER(".task");

        companion object {
            /**
             * Converts a PascalCase string name into the corresponding enum constant.
             * For example, "FaceDetector" becomes Type.FACE_DETECTOR.
             *
             * @param name The PascalCase name of the task.
             * @return The matching [Type] constant.
             * @throws IllegalArgumentException if the name does not match any known type.
             */
            fun fromName(name: String): Type {
                // Converts a name like "FaceDetector" to "FACE_DETECTOR"
                val enumName = name.replace(Regex("(?<=[a-z])(?=[A-Z])"), "_").uppercase()
                return try {
                    valueOf(enumName)
                } catch (e: IllegalArgumentException) {
                    // Provide a helpful error message if the mapping fails
                    throw IllegalArgumentException(
                        "No task type found for name '$name'. " +
                                "Available types are: ${values().joinToString { it.name }}"
                    )
                }
            }
        }
    }

    @get:Input
    @get:Optional
    abstract val defaultModel: Property<String>


    @get:Input
    @get:Optional
    abstract val models: ListProperty<String>
}
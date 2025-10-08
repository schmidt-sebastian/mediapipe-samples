package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.core.Quantization
import java.util.regex.Pattern

/** The public interface for a vision model. */
interface VisionModel {


    val task: VisionTask.Type
    val enumName: String
    val modelName: String

    val version: String

    val quantization: Quantization

    /** Creates the model URL from the model information. */
    fun createModelUrl(): String {
        val modelBaseName = modelName

        // Extracts the version number (e.g., "v1" -> "1").
        val versionNumber = version.removePrefix("v")

        // Determines the model file extension.
        val extension = task.modelExtension

        return "https://storage.googleapis.com/mediapipe-models/${task.name.lowercase()}/${modelName}/${quantization.name.lowercase()}/$versionNumber/${modelName}.$extension"
    }

    fun createModelFileName(): String {
        val modelBaseName = modelName

        // Extracts the version number (e.g., "v1" -> "1").
        val versionNumber = version.removePrefix("v")

        // Determines the model file extension.
        val extension = task.modelExtension

        return "${modelName}_v${versionNumber}_${quantization.description}.$extension"
    }

    companion object {
        @JvmStatic
        fun fromCanonicalName(task:VisionTask.Type, name: String): VisionModel {
            val pattern = Pattern.compile("^(.*)_(v\\d+)_(fp16|fp32|int8)$")
            val matcher = pattern.matcher(name)
            if (!matcher.matches()) {
                throw IllegalArgumentException("Unsupported model name: $name")
            }
            // Matcher groups are 1-based.
            val modelName = matcher.group(1)
            val version = matcher.group(2)
            val quantization = Quantization.fromCanonicalName(matcher.group(3))
            return object : VisionModel {
                override val task: VisionTask.Type = task
                override val enumName: String = name.uppercase()
                override val modelName: String = modelName
                override val version: String = version
                override val quantization: Quantization = quantization
            }
        }
    }
}

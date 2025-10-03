package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.core.Quantization
import java.util.regex.Pattern

/** The public interface for a vision model. */
interface VisionModel {
    fun task(): VisionTask.Type
  fun enumName(): String
  fun modelName(): String
  fun version(): String
  fun quantization(): Quantization

  /** Creates the model URL from the model information. */
fun createModelUrl(): String {
    val modelBaseName = modelName()

    // Extracts the version number (e.g., "v1" -> "1").
    val versionNumber = version().removePrefix("v")

    // Determines the model file extension.
    val extension = if (modelBaseName == "face_landmarker") "task" else "tflite"

    return "https://storage.googleapis.com/mediapipe-models/${task().name.lowercase()}/${modelName()}/${quantization().name.lowercase()}/$versionNumber/${modelName()}.$extension"
}

  companion object {
    @JvmStatic
    fun fromCanonicalName(taskName:String, modelName: String): VisionModel {
      val pattern = Pattern.compile("^(.*)_(v\\d+)_(fp16|fp32|int8)$")
      val matcher = pattern.matcher(modelName)
      if (!matcher.matches()) {
        throw IllegalArgumentException("Unsupported model name: $modelName")
      }
      // Matcher groups are 1-based.
      val modelName = matcher.group(1)
      val version = matcher.group(2)
      val quantization = Quantization.fromCanonicalName(matcher.group(3))
        val task = VisionTask.Type.fromName(taskName)
      return object : VisionModel {
          override fun task(): VisionTask.Type = task
        override fun enumName(): String = modelName().uppercase()
        override fun modelName(): String = modelName
        override fun version(): String = version
        override fun quantization(): Quantization = quantization
      }
    }
  }
}
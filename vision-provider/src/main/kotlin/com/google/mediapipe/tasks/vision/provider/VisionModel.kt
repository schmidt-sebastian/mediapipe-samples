package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.core.Quantization

/** The public interface for a vision model. */
interface VisionModel {
  fun task(): VisionTask.Type
  fun enumName(): String
  fun modelName(): String
  fun version(): String
  fun quantization(): Quantization

  fun getVariants() : List<String> {
   return listOf(
      "other",
      "mediatek_mt6878_android15",
      "mediatek_mt6897_android15",
      "mediatek_mt6983_android15",
      "mediatek_mt6985_android15",
      "mediatek_mt6989_android15",
      "mediatek_mt6991_android15",
      "qualcomm_sm8350",
      "qualcomm_sm8450",
      "qualcomm_sm8650",
      "qualcomm_sm8750"
    )
  }
  /** Creates the model URL from the model information. */
  fun createModelUrls(): List<String> {
    val modelVariants = getVariants()

      val baseName = modelName().split("_").dropLast(2).joinToString("_")

      val shortModelName = modelName().split("_").dropLast(2).joinToString("_") + "_" + modelName().split("_").last()
    val baseUrl =
      "https://huggingface.co/schmidt-sebastian/${baseName}_${version()}/resolve/main/"

    return modelVariants.map { variant ->
      val modelNameWithVariant = if (variant != "other") "${shortModelName}_$variant" else shortModelName
      val fileName = "$modelNameWithVariant.tflite"
      "$baseUrl$fileName"
    }
  }

  companion object {
    @JvmStatic
    fun fromCanonicalName(taskName: String, modelName: String): VisionModel {
      val task = VisionTask.Type.fromName(taskName)
      // Infer quantization directly from the model name string.
      val extractedQuantization = when {
        modelName.contains("_fp32") -> Quantization.FLOAT32
        modelName.contains("_fp16") -> Quantization.FLOAT16
        modelName.contains("_int8") -> Quantization.INT8
        else -> throw IllegalArgumentException("Cannot determine quantization from model name: $modelName")
      }

      return object : VisionModel {
        override fun task(): VisionTask.Type = task
        override fun enumName(): String = modelName.uppercase().replace("_", "")
        override fun modelName(): String = modelName // Returns the full name, e.g., "magic_touch_fp32_qualcomm_sm8350"
        override fun version(): String = "v1" // Version is static ("v1") based on the URL path.
        override fun quantization(): Quantization = extractedQuantization
      }
    }
  }
}
package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.ErrorListener
import com.google.mediapipe.tasks.core.OutputHandler
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenterResult

public class ImageSegmenterSettingsInternal @JvmOverloads constructor(
    private val outputConfidenceMasks: Boolean = VisionProviderBase.Companion.DEFAULT_OUTPUT_CONFIDENCE_MASKS,
    private val outputCategoryMask: Boolean = VisionProviderBase.Companion.DEFAULT_OUTPUT_CATEGORY_MASK,
    private val displayNamesLocale: String? = VisionProviderBase.Companion.DEFAULT_DISPLAY_NAMES_LOCALE,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE,
    private val resultListener: OutputHandler.ResultListener<ImageSegmenterResult, MPImage>? = null,
    private val errorListener: ErrorListener? = null,
) {
    fun outputConfidenceMasks(): Boolean {
        return outputConfidenceMasks
    }

    fun outputCategoryMask(): Boolean {
        return outputCategoryMask
    }

    fun displayNamesLocale(): String? {
        return displayNamesLocale
    }

    fun runningMode(): RunningMode {
        return runningMode
    }

    fun resultListener(): OutputHandler.ResultListener<ImageSegmenterResult, MPImage>? {
        return resultListener
    }

    fun errorListener(): ErrorListener? {
        return errorListener
    }
}
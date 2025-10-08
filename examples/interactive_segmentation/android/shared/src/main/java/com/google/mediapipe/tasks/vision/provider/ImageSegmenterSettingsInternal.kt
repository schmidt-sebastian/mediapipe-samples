package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.vision.core.RunningMode

public class ImageSegmenterSettingsInternal @JvmOverloads constructor(
    private val outputConfidenceMasks: Boolean = VisionProviderBase.Companion.DEFAULT_OUTPUT_CONFIDENCE_MASKS,
    private val outputCategoryMask: Boolean = VisionProviderBase.Companion.DEFAULT_OUTPUT_CATEGORY_MASK,
    private val displayNamesLocale: String? = VisionProviderBase.Companion.DEFAULT_DISPLAY_NAMES_LOCALE,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE
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
}
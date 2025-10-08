package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.vision.core.RunningMode

public class InteractiveSegmenterSettingsInternal @JvmOverloads constructor(
    private val outputConfidenceMasks: Boolean = VisionProviderBase.Companion.DEFAULT_OUTPUT_CONFIDENCE_MASKS,
    private val outputCategoryMask: Boolean = VisionProviderBase.Companion.DEFAULT_OUTPUT_CATEGORY_MASK,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE
) {
    fun outputConfidenceMasks(): Boolean {
        return outputConfidenceMasks
    }

    fun outputCategoryMask(): Boolean {
        return outputCategoryMask
    }

    fun runningMode(): RunningMode {
        return runningMode
    }
}
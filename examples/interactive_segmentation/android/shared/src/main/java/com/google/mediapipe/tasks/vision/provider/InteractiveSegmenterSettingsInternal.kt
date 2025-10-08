package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.vision.core.RunningMode

public class InteractiveSegmenterSettingsInternal @JvmOverloads constructor(
    private val outputConfidenceMasks: Boolean = false,
    private val outputCategoryMask: Boolean = false,
    private val runningMode: RunningMode = RunningMode.IMAGE
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
package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.vision.core.RunningMode

public class ImageClassifierSettingsInternal @JvmOverloads constructor(
    private val displayNamesLocale: String? = VisionProviderBase.Companion.DEFAULT_DISPLAY_NAMES_LOCALE,
    private val maxResults: Int = VisionProviderBase.Companion.UNLIMITED_RESULTS,
    private val scoreThreshold: Float = 0.0f,
    private val categoryAllowlist: List<String>? = null,
    private val categoryDenylist: List<String>? = null,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE
) {
    fun displayNamesLocale(): String? {
        return displayNamesLocale
    }

    fun maxResults(): Int {
        return maxResults
    }

    fun scoreThreshold(): Float {
        return scoreThreshold
    }

    fun categoryAllowlist(): List<String>? {
        return categoryAllowlist
    }

    fun categoryDenylist(): List<String>? {
        return categoryDenylist
    }

    fun runningMode(): RunningMode {
        return runningMode
    }
}
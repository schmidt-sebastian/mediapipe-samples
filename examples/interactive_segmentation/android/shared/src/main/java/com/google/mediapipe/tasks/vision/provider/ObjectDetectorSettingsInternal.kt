package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.vision.core.RunningMode

public class ObjectDetectorSettingsInternal @JvmOverloads constructor(
    private val displayNamesLocale: String? = "en",
    private val maxResults: Int = -1,
    private val scoreThreshold: Float = 0.5f,
    private val categoryAllowlist: List<String>? = null,
    private val categoryDenylist: List<String>? = null,
    private val runningMode: RunningMode = RunningMode.IMAGE
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
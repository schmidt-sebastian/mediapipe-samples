package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.vision.core.RunningMode

public class ImageEmbedderSettingsInternal @JvmOverloads constructor(
    private val l2Normalize: Boolean = false,
    private val quantize: Boolean = false,
    private val runningMode: RunningMode = RunningMode.IMAGE
) {
    fun l2Normalize(): Boolean {
        return l2Normalize
    }

    fun quantize(): Boolean {
        return quantize
    }

    fun runningMode(): RunningMode {
        return runningMode
    }
}
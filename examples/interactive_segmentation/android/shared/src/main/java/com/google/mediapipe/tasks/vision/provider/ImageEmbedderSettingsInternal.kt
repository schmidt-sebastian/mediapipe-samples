package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.vision.core.RunningMode

public class ImageEmbedderSettingsInternal @JvmOverloads constructor(
    private val l2Normalize: Boolean = VisionProviderBase.Companion.DEFAULT_L2_NORMALIZE,
    private val quantize: Boolean = VisionProviderBase.Companion.DEFAULT_QUANTIZE,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE
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
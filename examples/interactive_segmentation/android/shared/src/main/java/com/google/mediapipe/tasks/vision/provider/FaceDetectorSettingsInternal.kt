package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.vision.core.RunningMode

public class FaceDetectorSettingsInternal @JvmOverloads constructor(
    private val minDetectionConfidence: Float = 0.5f,
    private val minSuppressionThreshold: Float = 0.5f,
    private val runningMode: RunningMode = RunningMode.IMAGE
) {
    fun minDetectionConfidence(): Float {
        return minDetectionConfidence
    }

    fun minSuppressionThreshold(): Float {
        return minSuppressionThreshold
    }

    fun runningMode(): RunningMode {
        return runningMode
    }
}
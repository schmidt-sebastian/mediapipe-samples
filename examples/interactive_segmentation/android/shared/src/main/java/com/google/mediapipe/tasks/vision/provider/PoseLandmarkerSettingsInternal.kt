package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.vision.core.RunningMode

public class PoseLandmarkerSettingsInternal @JvmOverloads constructor(
    private val minPoseDetectionConfidence: Float = 0.5f,
    private val minPosePresenceConfidence: Float = 0.5f,
    private val minTrackingConfidence: Float = 0.5f,
    private val numPoses: Int = 1,
    private val outputSegmentationMasks: Boolean = false,
    private val runningMode: RunningMode = RunningMode.IMAGE
) {
    fun minPoseDetectionConfidence(): Float {
        return minPoseDetectionConfidence
    }

    fun minPosePresenceConfidence(): Float {
        return minPosePresenceConfidence
    }

    fun minTrackingConfidence(): Float {
        return minTrackingConfidence
    }

    fun numPoses(): Int {
        return numPoses
    }

    fun outputSegmentationMasks(): Boolean {
        return outputSegmentationMasks
    }

    fun runningMode(): RunningMode {
        return runningMode
    }
}
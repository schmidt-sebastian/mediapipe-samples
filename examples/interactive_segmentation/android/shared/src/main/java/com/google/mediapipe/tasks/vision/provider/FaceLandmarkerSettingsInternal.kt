package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.vision.core.RunningMode

public class FaceLandmarkerSettingsInternal @JvmOverloads constructor(
    private val minFaceDetectionConfidence: Float = 0.5f,
    private val minFacePresenceConfidence: Float = 0.5f,
    private val minTrackingConfidence: Float = 0.5f,
    private val numFaces: Int = 1,
    private val outputFaceBlendshapes: Boolean = false,
    private val outputFacialTransformationMatrixes: Boolean = false,
    private val runningMode: RunningMode = RunningMode.IMAGE
) {
    fun minFaceDetectionConfidence(): Float {
        return minFaceDetectionConfidence
    }

    fun minFacePresenceConfidence(): Float {
        return minFacePresenceConfidence
    }

    fun minTrackingConfidence(): Float {
        return minTrackingConfidence
    }

    fun numFaces(): Int {
        return numFaces
    }

    fun outputFaceBlendshapes(): Boolean {
        return outputFaceBlendshapes
    }

    fun outputFacialTransformationMatrixes(): Boolean {
        return outputFacialTransformationMatrixes
    }

    fun runningMode(): RunningMode {
        return runningMode
    }
}
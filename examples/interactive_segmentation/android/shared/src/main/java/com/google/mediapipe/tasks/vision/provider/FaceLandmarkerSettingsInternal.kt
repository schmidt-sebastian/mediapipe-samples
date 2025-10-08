package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.vision.core.RunningMode

public class FaceLandmarkerSettingsInternal @JvmOverloads constructor(
    private val minFaceDetectionConfidence: Float = 0.5f,
    private val minFacePresenceConfidence: Float = 0.5f,
    private val minTrackingConfidence: Float = 0.5f,
    private val numFaces: Int = 1,
    private val outputFaceBlendshapes: Boolean = VisionProviderBase.Companion.DEFAULT_OUTPUT_BLENDSHAPES,
    private val outputFacialTransformationMatrixes: Boolean = VisionProviderBase.Companion.DEFAULT_OUTPUT_FACIAL_TRANSFORMATION_MATRIXES,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE
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
package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.ErrorListener
import com.google.mediapipe.tasks.core.OutputHandler
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult

public class FaceDetectorSettingsInternal @JvmOverloads constructor(
    private val minDetectionConfidence: Float = 0.5f,
    private val minSuppressionThreshold: Float = 0.5f,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE,
    private val resultListener: OutputHandler.ResultListener<FaceDetectorResult, MPImage>? = null,
    private val errorListener: ErrorListener? = null,
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

    fun resultListener(): OutputHandler.ResultListener<FaceDetectorResult, MPImage>? {
        return resultListener
    }

    fun errorListener(): ErrorListener? {
        return errorListener
    }
}
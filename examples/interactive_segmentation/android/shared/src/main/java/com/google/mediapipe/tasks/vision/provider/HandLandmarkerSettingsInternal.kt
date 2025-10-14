package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.ErrorListener
import com.google.mediapipe.tasks.core.OutputHandler
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

public class HandLandmarkerSettingsInternal @JvmOverloads constructor(
    private val minHandDetectionConfidence: Float = 0.5f,
    private val minHandPresenceConfidence: Float = 0.5f,
    private val minTrackingConfidence: Float = 0.5f,
    private val numHands: Int = 1,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE,
    private val resultListener: OutputHandler.ResultListener<HandLandmarkerResult, MPImage>? = null,
    private val errorListener: ErrorListener? = null,
) {
    fun minHandDetectionConfidence(): Float {
        return minHandDetectionConfidence
    }

    fun minHandPresenceConfidence(): Float {
        return minHandPresenceConfidence
    }

    fun minTrackingConfidence(): Float {
        return minTrackingConfidence
    }

    fun numHands(): Int {
        return numHands
    }

    fun runningMode(): RunningMode {
        return runningMode
    }

    fun resultListener(): OutputHandler.ResultListener<HandLandmarkerResult, MPImage>? {
        return resultListener
    }

    fun errorListener(): ErrorListener? {
        return errorListener
    }
}
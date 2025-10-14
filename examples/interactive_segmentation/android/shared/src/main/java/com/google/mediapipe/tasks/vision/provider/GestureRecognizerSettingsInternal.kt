package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.processors.ClassifierOptions
import com.google.mediapipe.tasks.core.ErrorListener
import com.google.mediapipe.tasks.core.OutputHandler
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult

public class GestureRecognizerSettingsInternal @JvmOverloads constructor(
    private val minHandDetectionConfidence: Float = 0.5f,
    private val minHandPresenceConfidence: Float = 0.5f,
    private val minTrackingConfidence: Float = 0.5f,
    private val numHands: Int = 1,
    private val cannedGesturesClassifierOptions: ClassifierOptions? = null,
    private val customGesturesClassifierOptions: ClassifierOptions? = null,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE,
    private val resultListener: OutputHandler.ResultListener<GestureRecognizerResult, MPImage>? = null,
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

    fun cannedGesturesClassifierOptions(): ClassifierOptions? {
        return cannedGesturesClassifierOptions
    }

    fun customGesturesClassifierOptions(): ClassifierOptions? {
        return customGesturesClassifierOptions
    }

    fun runningMode(): RunningMode {
        return runningMode
    }

    fun resultListener(): OutputHandler.ResultListener<GestureRecognizerResult, MPImage>? {
        return resultListener
    }

    fun errorListener(): ErrorListener? {
        return errorListener
    }
}
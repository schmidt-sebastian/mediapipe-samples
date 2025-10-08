package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.components.processors.ClassifierOptions
import com.google.mediapipe.tasks.vision.core.RunningMode

public class GestureRecognizerSettingsInternal @JvmOverloads constructor(
    private val minHandDetectionConfidence: Float = 0.5f,
    private val minHandPresenceConfidence: Float = 0.5f,
    private val minTrackingConfidence: Float = 0.5f,
    private val numHands: Int = 1,
    private val cannedGesturesClassifierOptions: ClassifierOptions? = null,
    private val customGesturesClassifierOptions: ClassifierOptions? = null,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE
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
}
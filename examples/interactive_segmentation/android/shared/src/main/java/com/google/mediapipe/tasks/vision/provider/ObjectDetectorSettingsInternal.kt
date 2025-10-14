package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.ErrorListener
import com.google.mediapipe.tasks.core.OutputHandler
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult

public class ObjectDetectorSettingsInternal @JvmOverloads constructor(
    private val displayNamesLocale: String? = VisionProviderBase.Companion.DEFAULT_DISPLAY_NAMES_LOCALE,
    private val maxResults: Int = VisionProviderBase.Companion.UNLIMITED_RESULTS,
    private val scoreThreshold: Float = 0.5f,
    private val categoryAllowlist: List<String>? = null,
    private val categoryDenylist: List<String>? = null,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE,
    private val resultListener: OutputHandler.ResultListener<ObjectDetectorResult, MPImage>? = null,
    private val errorListener: ErrorListener? = null,
) {
    fun displayNamesLocale(): String? {
        return displayNamesLocale
    }

    fun maxResults(): Int {
        return maxResults
    }

    fun scoreThreshold(): Float {
        return scoreThreshold
    }

    fun categoryAllowlist(): List<String>? {
        return categoryAllowlist
    }

    fun categoryDenylist(): List<String>? {
        return categoryDenylist
    }

    fun runningMode(): RunningMode {
        return runningMode
    }

    fun resultListener(): OutputHandler.ResultListener<ObjectDetectorResult, MPImage>? {
        return resultListener
    }

    fun errorListener(): ErrorListener? {
        return errorListener
    }
}
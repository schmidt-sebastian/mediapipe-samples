package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.ErrorListener
import com.google.mediapipe.tasks.core.OutputHandler
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

public class PoseLandmarkerSettingsInternal @JvmOverloads constructor(
    private val minPoseDetectionConfidence: Float = 0.5f,
    private val minPosePresenceConfidence: Float = 0.5f,
    private val minTrackingConfidence: Float = 0.5f,
    private val numPoses: Int = 1,
    private val outputSegmentationMasks: Boolean = VisionProviderBase.Companion.DEFAULT_OUTPUT_SEGMENTATION_MASKS,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE,
    private val resultListener: OutputHandler.ResultListener<PoseLandmarkerResult, MPImage>? = null,
    private val errorListener: ErrorListener? = null,
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

    fun resultListener(): OutputHandler.ResultListener<PoseLandmarkerResult, MPImage>? {
        return resultListener

    }

    fun errorListener(): ErrorListener? {
        return errorListener
    }
}
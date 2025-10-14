package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.ErrorListener
import com.google.mediapipe.tasks.core.OutputHandler
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedderResult

public class ImageEmbedderSettingsInternal @JvmOverloads constructor(
    private val l2Normalize: Boolean = VisionProviderBase.Companion.DEFAULT_L2_NORMALIZE,
    private val quantize: Boolean = VisionProviderBase.Companion.DEFAULT_QUANTIZE,
    private val runningMode: RunningMode = VisionProviderBase.Companion.DEFAULT_RUNNING_MODE,
    private val resultListener: OutputHandler.ResultListener<ImageEmbedderResult, MPImage>? = null,
    private val errorListener: ErrorListener? = null,
) {
    fun l2Normalize(): Boolean {
        return l2Normalize
    }

    fun quantize(): Boolean {
        return quantize
    }

    fun runningMode(): RunningMode {
        return runningMode
    }

    fun resultListener(): OutputHandler.ResultListener<ImageEmbedderResult, MPImage>? {
        return resultListener
    }

    fun errorListener(): ErrorListener? {
        return errorListener
    }
}
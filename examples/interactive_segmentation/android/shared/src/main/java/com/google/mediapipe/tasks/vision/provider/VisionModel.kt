package com.google.mediapipe.tasks.vision.provider

import com.google.mediapipe.tasks.core.Quantization

interface VisionModel {
    val enumName: String?
    val modelName: String?

    val version: String?

    val quantization: Quantization?
}
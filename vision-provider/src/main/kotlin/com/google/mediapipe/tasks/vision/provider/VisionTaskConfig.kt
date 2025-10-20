package com.google.mediapipe.tasks.vision.provider

import com.google.gson.annotations.SerializedName


/**
 * Represents the configuration for a single MediaPipe vision task.
 */
data class VisionTaskConfig(
    @SerializedName("defaultModel")
    val defaultModel: String,

    @SerializedName("models")
    val models: List<String>,

    @SerializedName("delivery")
    val delivery: String
)
package com.google.mediapipe.tasks.vision.provider

import com.google.gson.annotations.SerializedName

/**
 * A wrapper class that matches the root structure of the JSON file.
 * It contains a map of all vision task configurations.
 */
data class TasksWrapper(
    @SerializedName("compileSdk")
    val compileSdk: Int,

    @SerializedName("minSdk")
    val minSdk: Int,

    @SerializedName("npuModuleDelivery")
    val npuModuleDelivery: String,

    @SerializedName("deviceTargetingConfiguration")
    val deviceTargetingConfiguration: String,

    @SerializedName("tasks")
    val tasks: Map<String, VisionTaskConfig>
)

package com.google.mediapipe.tasks.vision.provider
sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Downloading(val progress: Int) : DownloadStatus()
    data class Completed(val path: String) : DownloadStatus()
    data class Failed(val errorCode: Int) : DownloadStatus()
}
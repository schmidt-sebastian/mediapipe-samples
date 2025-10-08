package com.google.mediapipe.tasks.vision.provider

import android.app.Activity
import android.content.Context
import com.google.android.play.core.assetpacks.AssetPackManager
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackState
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener
import com.google.android.play.core.assetpacks.model.AssetPackStatus

class AIPackDownloader(context: Context) {

    interface DownloadListener {
        fun onStatusUpdate(status: DownloadStatus)
        fun onShowConfirmationDialog(activity: Activity, status: AssetPackState)
    }

    private val assetPackManager: AssetPackManager = AssetPackManagerFactory.getInstance(context)
    private var listener: DownloadListener? = null
    private var packName: String? = null

    private val assetPackStateUpdateListener = AssetPackStateUpdateListener { state ->
        packName?.let {
            if (state.name() == it) {
                updateDownloadStatus(state)
            }
        }
    }

    fun setListener(listener: DownloadListener) {
        this.listener = listener
        assetPackManager.registerListener(assetPackStateUpdateListener)
    }

    fun removeListener() {
        assetPackManager.unregisterListener(assetPackStateUpdateListener)
        this.listener = null
    }

    fun downloadPack(packName: String) {
        this.packName = packName
        assetPackManager.fetch(listOf(packName))
    }

    private fun updateDownloadStatus(state: AssetPackState) {
        when (state.status()) {
            AssetPackStatus.PENDING -> listener?.onStatusUpdate(DownloadStatus.Downloading(0.0F))
            AssetPackStatus.DOWNLOADING -> {
                val progress = state.bytesDownloaded() * 1.0F / state.totalBytesToDownload()
                listener?.onStatusUpdate(DownloadStatus.Downloading(progress))
            }
            AssetPackStatus.COMPLETED -> {
                val packPath = assetPackManager.getPackLocation(state.name())?.path()
                if (packPath != null) {
                    listener?.onStatusUpdate(DownloadStatus.Completed(packPath))
                } else {
                    listener?.onStatusUpdate(DownloadStatus.Failed(state.errorCode()))
                }
            }
            AssetPackStatus.FAILED, AssetPackStatus.CANCELED -> {
                listener?.onStatusUpdate(DownloadStatus.Failed(state.errorCode()))
            }
            AssetPackStatus.WAITING_FOR_WIFI -> {
                // You can optionally show a dialog to the user to ask for permission to download over cellular data.
                // In this example, we'll just treat it as a downloading state.
                listener?.onStatusUpdate(DownloadStatus.Downloading(0.0F))
            }
            AssetPackStatus.REQUIRES_USER_CONFIRMATION -> {
                // In this state, you must show a confirmation dialog to the user.
                // You can get the activity from your calling class.
                // For this example, we will just notify the listener to handle this case
            }
            AssetPackStatus.NOT_INSTALLED, AssetPackStatus.UNKNOWN -> {
                listener?.onStatusUpdate(DownloadStatus.Idle)
            }
        }
    }

    fun requestCellularDataDownload(activity: Activity) {
        packName?.let {
            assetPackManager.showCellularDataConfirmation(activity)
                .addOnSuccessListener {
                    // User has approved the download over cellular data.
                    // The download will now proceed.
                }
        }
    }
}
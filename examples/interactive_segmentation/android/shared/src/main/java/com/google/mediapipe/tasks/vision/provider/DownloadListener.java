package com.google.mediapipe.tasks.vision.provider;

import androidx.annotation.NonNull;

/**
 * Listener for receiving model download status updates.
 * This is used for models managed by AIPack.
 */
public interface DownloadListener {
    /**
     * Called when download progress is updated.
     */
    void onProgress(float progress);

    /**
     * Called when the model has been successfully downloaded.
     */
    void onCompleted();

    /**
     * Called when the model download fails.
     */
    void onFailed( @NonNull Exception e);

    //fun onShowConfirmationDialog(activity: Activity, status: AssetPackState) {
}

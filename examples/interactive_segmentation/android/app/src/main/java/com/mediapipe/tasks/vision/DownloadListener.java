package com.mediapipe.tasks.vision;

import androidx.annotation.NonNull;

/**
 * Listener for receiving model download status updates.
 * This is used for models managed by AIPack.
 */
public interface DownloadListener {
    /**
     * Called when download progress is updated.
     */
    void onProgress(@NonNull VisionModel model, float progress);

    /**
     * Called when the model has been successfully downloaded.
     */
    void onCompleted(@NonNull VisionModel model);

    /**
     * Called when the model download fails.
     */
    void onFailed(@NonNull VisionModel model, @NonNull Exception e);
}

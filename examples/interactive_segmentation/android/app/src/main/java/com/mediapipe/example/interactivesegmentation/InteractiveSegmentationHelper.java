/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mediapipe.example.interactivesegmentation;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.ByteBufferExtractor;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedKeypoint;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter;
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenterResult;
import com.google.mediapipe.tasks.vision.interactivesegmenter.InteractiveSegmenter;
import com.google.mediapipe.tasks.vision.interactivesegmenter.InteractiveSegmenter.RegionOfInterest;
import com.google.mediapipe.tasks.vision.provider.VisionProvider;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class InteractiveSegmentationHelper {

    private static final String TAG = "InteractiveSegmentationHelper";

    private final Context context;
    private final InteractiveSegmentationListener listener;

    private InteractiveSegmenter interactiveSegmenter;
    private Bitmap inputImage;

    public InteractiveSegmentationHelper(
    Context context,
    InteractiveSegmentationListener listener
    ) {
        this.context = context;
        this.listener = listener;
        setupInteractiveSegmenter();
    }

    public void clear() {
        if (interactiveSegmenter != null) {
            interactiveSegmenter.close();
            interactiveSegmenter = null;
        }
    }

    private void setupInteractiveSegmenter() {
        VisionProvider provider = VisionProvider.create(context);

        try {
            VisionProvider.InteractiveSegmenterSettings settings = new VisionProvider.InteractiveSegmenterSettings()
                    .withResultListener(this::returnSegmenterResults)
                    .withErrorListener(this::returnSegmenterError);
            interactiveSegmenter = provider.createInteractiveSegmenter(VisionProvider.InteractiveSegmenterModel.MAGIC_TOUCH_V1_FP32, settings).get();
        } catch (IllegalStateException | ExecutionException | InterruptedException e) {
            listener.onError(
                "Interactive segmentation failed to initialize. See error logs for details"
            );
            Log.e(
                TAG,
                "MP Task Vision failed to load the task with error: " + e.getMessage()
            );
        }
    }

    /**
     * Prepares input bitmap for segmentation
     */
    public void setInputImage(Bitmap bitmap) {
        this.inputImage = bitmap;
    }

    public boolean isInputImageAssigned() {
        return inputImage != null;
    }

    /**
     * Runs segmentation on an image using a custom ROI (region of interest)
     */
    public void segment(float normX, float normY) {
        if (inputImage == null) {
            return;
        }

        // According to the MediaPipe documentation, the segmenter must be created for each session.
        clear();
        setupInteractiveSegmenter();

        if (interactiveSegmenter == null) {
            return;
        }

        RegionOfInterest roi = RegionOfInterest.create(
                NormalizedKeypoint.create(
                    normX * inputImage.getWidth(),
                    normY * inputImage.getHeight()
                )
                );
        MPImage mpImage = new BitmapImageBuilder(inputImage).build();

        // The segmentAsync method requires a listener.
        interactiveSegmenter.segmentWithResultListener(
            mpImage,
            roi
        );
    }

    /**
     * Returns the result of segmentation as a ByteBuffer
     */
    private void returnSegmenterResults(
    ImageSegmenterResult result,
    MPImage mpImage
    ) {
        // Extract first MPImage and convert to byte buffer to display
        ByteBuffer byteBuffer = ByteBufferExtractor.extract(
                result.categoryMask().get()
                );

        ResultBundle resultBundle = new ResultBundle(
            byteBuffer,
            mpImage.getWidth(),
            mpImage.getHeight()
        );
        listener.onResults(resultBundle);
    }

    private void returnSegmenterError(RuntimeException error) {
        listener.onError(error.getMessage());
    }

    public static class ResultBundle {
        public final ByteBuffer byteBuffer;
        public final int maskWidth;
        public final int maskHeight;

        public ResultBundle(ByteBuffer byteBuffer, int maskWidth, int maskHeight) {
            this.byteBuffer = byteBuffer;
            this.maskWidth = maskWidth;
            this.maskHeight = maskHeight;
        }
    }

    public interface InteractiveSegmentationListener {
        void onError(String error);
        void onResults(ResultBundle result);
    }
}
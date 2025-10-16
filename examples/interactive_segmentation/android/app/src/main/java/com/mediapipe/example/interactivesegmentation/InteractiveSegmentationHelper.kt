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
package com.mediapipe.example.interactivesegmentation

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.ByteBufferExtractor
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedKeypoint
import com.google.mediapipe.tasks.core.ErrorListener
import com.google.mediapipe.tasks.core.OutputHandler.ResultListener
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenterResult
import com.google.mediapipe.tasks.vision.interactivesegmenter.InteractiveSegmenter
import com.google.mediapipe.tasks.vision.provider.DownloadListener
import com.google.mediapipe.tasks.vision.provider.VisionProvider
import com.google.mediapipe.tasks.vision.provider.VisionProvider.Companion.create
import com.google.mediapipe.tasks.vision.provider.VisionProvider.InteractiveSegmenterSettings
import java.nio.ByteBuffer
import java.util.concurrent.ExecutionException

class InteractiveSegmentationHelper(
    private val context: Context,
    private val listener: InteractiveSegmentationListener
) {
    private var interactiveSegmenter: InteractiveSegmenter? = null
    private var inputImage: Bitmap? = null

    init {
        setupInteractiveSegmenter()
    }

    fun clear() {
        if (interactiveSegmenter != null) {
            interactiveSegmenter!!.close()
            interactiveSegmenter = null
        }
    }

    private fun setupInteractiveSegmenter() {
        val provider = create(context)

        try {
            val settings = InteractiveSegmenterSettings()
                .withResultListener(ResultListener { result: ImageSegmenterResult?, mpImage: MPImage? ->
                    this.returnSegmenterResults(
                        result!!, mpImage!!
                    )
                })
                .withErrorListener(ErrorListener { error: RuntimeException? ->
                    this.returnSegmenterError(
                        error!!
                    )
                })
            provider.addDownloadListener(
                VisionProvider.InteractiveSegmenterModel.MAGIC_TOUCH_V1_FP32,
                object : DownloadListener {
                    override fun onProgress(progress: Float) {
                    }

                    override fun onCompleted() {
                    }

                    override fun onFailed(e: Exception) {
                    }
                })
            interactiveSegmenter = provider.createInteractiveSegmenter(
                VisionProvider.InteractiveSegmenterModel.MAGIC_TOUCH_V1_FP32,
                settings
            ).get()
        } catch (e: IllegalStateException) {
            listener.onError(
                "Interactive segmentation failed to initialize. See error logs for details"
            )
            Log.e(
                TAG,
                "MP Task Vision failed to load the task with error: " + e.message
            )
        } catch (e: ExecutionException) {
            listener.onError(
                "Interactive segmentation failed to initialize. See error logs for details"
            )
            Log.e(
                TAG,
                "MP Task Vision failed to load the task with error: " + e.message
            )
        } catch (e: InterruptedException) {
            listener.onError(
                "Interactive segmentation failed to initialize. See error logs for details"
            )
            Log.e(
                TAG,
                "MP Task Vision failed to load the task with error: " + e.message
            )
        }
    }

    /**
     * Prepares input bitmap for segmentation
     */
    fun setInputImage(bitmap: Bitmap?) {
        this.inputImage = bitmap
    }

    val isInputImageAssigned: Boolean
        get() = inputImage != null

    /**
     * Runs segmentation on an image using a custom ROI (region of interest)
     */
    fun segment(normX: Float, normY: Float) {
        if (inputImage == null) {
            return
        }



        if (interactiveSegmenter == null) {
            return
        }

        val roi = InteractiveSegmenter.RegionOfInterest.create(
            NormalizedKeypoint.create(
                normX * inputImage!!.getWidth(),
                normY * inputImage!!.getHeight()
            )
        )
        val mpImage = BitmapImageBuilder(inputImage).build()

        // The segmentAsync method requires a listener.
        interactiveSegmenter!!.segmentWithResultListener(
            mpImage,
            roi
        )
    }

    /**
     * Returns the result of segmentation as a ByteBuffer
     */
    private fun returnSegmenterResults(
        result: ImageSegmenterResult,
        mpImage: MPImage
    ) {
        // Extract first MPImage and convert to byte buffer to display
        val byteBuffer = ByteBufferExtractor.extract(
            result.categoryMask().get()
        )

        val resultBundle = ResultBundle(
            byteBuffer,
            mpImage.getWidth(),
            mpImage.getHeight()
        )
        listener.onResults(resultBundle)
    }

    private fun returnSegmenterError(error: RuntimeException) {
        listener.onError(error.message)
    }

    class ResultBundle(val byteBuffer: ByteBuffer?, val maskWidth: Int, val maskHeight: Int)

    interface InteractiveSegmentationListener {
        fun onError(error: String?)
        fun onResults(result: ResultBundle?)
    }

    companion object {
        private const val TAG = "InteractiveSegmentationHelper"
    }
}
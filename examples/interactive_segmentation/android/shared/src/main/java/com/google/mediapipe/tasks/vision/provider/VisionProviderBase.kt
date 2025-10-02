package com.google.mediapipe.tasks.vision.provider

import android.content.Context
import android.webkit.DownloadListener
import com.google.mediapipe.tasks.components.processors.ClassifierOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedder
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter
import com.google.mediapipe.tasks.vision.interactivesegmenter.InteractiveSegmenter
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import java.util.concurrent.Future

open class VisionProviderBase(private val context: Context?) {
    public class FaceDetectorSettingsInternal @JvmOverloads constructor(
        private val minDetectionConfidence: Float = DEFAULT_CONFIDENCE,
        private val minSuppressionThreshold: Float = DEFAULT_CONFIDENCE,
        private val runningMode: RunningMode = DEFAULT_RUNNING_MODE
    ) {
        fun minDetectionConfidence(): Float {
            return minDetectionConfidence
        }

        fun minSuppressionThreshold(): Float {
            return minSuppressionThreshold
        }

        fun runningMode(): RunningMode {
            return runningMode
        }
    }

    public class FaceLandmarkerSettingsInternal @JvmOverloads constructor(
        private val minFaceDetectionConfidence: Float = DEFAULT_CONFIDENCE,
        private val minFacePresenceConfidence: Float = DEFAULT_CONFIDENCE,
        private val minTrackingConfidence: Float = DEFAULT_CONFIDENCE,
        private val numFaces: Int = DEFAULT_NUM_RESULTS,
        private val outputFaceBlendshapes: Boolean = DEFAULT_OUTPUT_BLENDSHAPES,
        private val outputFacialTransformationMatrixes: Boolean = DEFAULT_OUTPUT_FACIAL_TRANSFORMATION_MATRIXES,
        private val runningMode: RunningMode = DEFAULT_RUNNING_MODE
    ) {
        fun minFaceDetectionConfidence(): Float {
            return minFaceDetectionConfidence
        }

        fun minFacePresenceConfidence(): Float {
            return minFacePresenceConfidence
        }

        fun minTrackingConfidence(): Float {
            return minTrackingConfidence
        }

        fun numFaces(): Int {
            return numFaces
        }

        fun outputFaceBlendshapes(): Boolean {
            return outputFaceBlendshapes
        }

        fun outputFacialTransformationMatrixes(): Boolean {
            return outputFacialTransformationMatrixes
        }

        fun runningMode(): RunningMode {
            return runningMode
        }
    }

    public class GestureRecognizerSettingsInternal @JvmOverloads constructor(
        private val minHandDetectionConfidence: Float = DEFAULT_CONFIDENCE,
        private val minHandPresenceConfidence: Float = DEFAULT_CONFIDENCE,
        private val minTrackingConfidence: Float = DEFAULT_CONFIDENCE,
        private val numHands: Int = DEFAULT_NUM_RESULTS,
        private val cannedGesturesClassifierOptions: ClassifierOptions? = null,
        private val customGesturesClassifierOptions: ClassifierOptions? = null,
        private val runningMode: RunningMode = DEFAULT_RUNNING_MODE
    ) {
        fun minHandDetectionConfidence(): Float {
            return minHandDetectionConfidence
        }

        fun minHandPresenceConfidence(): Float {
            return minHandPresenceConfidence
        }

        fun minTrackingConfidence(): Float {
            return minTrackingConfidence
        }

        fun numHands(): Int {
            return numHands
        }

        fun cannedGesturesClassifierOptions(): ClassifierOptions? {
            return cannedGesturesClassifierOptions
        }

        fun customGesturesClassifierOptions(): ClassifierOptions? {
            return customGesturesClassifierOptions
        }

        fun runningMode(): RunningMode {
            return runningMode
        }
    }

    public class HandLandmarkerSettingsInternal @JvmOverloads constructor(
        private val minHandDetectionConfidence: Float = DEFAULT_CONFIDENCE,
        private val minHandPresenceConfidence: Float = DEFAULT_CONFIDENCE,
        private val minTrackingConfidence: Float = DEFAULT_CONFIDENCE,
        private val numHands: Int = DEFAULT_NUM_RESULTS,
        private val runningMode: RunningMode = DEFAULT_RUNNING_MODE
    ) {
        fun minHandDetectionConfidence(): Float {
            return minHandDetectionConfidence
        }

        fun minHandPresenceConfidence(): Float {
            return minHandPresenceConfidence
        }

        fun minTrackingConfidence(): Float {
            return minTrackingConfidence
        }

        fun numHands(): Int {
            return numHands
        }

        fun runningMode(): RunningMode {
            return runningMode
        }
    }

    public class ImageClassifierSettingsInternal @JvmOverloads constructor(
        private val displayNamesLocale: String? = DEFAULT_DISPLAY_NAMES_LOCALE,
        private val maxResults: Int = UNLIMITED_RESULTS,
        private val scoreThreshold: Float = 0.0f,
        private val categoryAllowlist: List<String>? = null,
        private val categoryDenylist: List<String>? = null,
        private val runningMode: RunningMode = DEFAULT_RUNNING_MODE
    ) {
        fun displayNamesLocale(): String? {
            return displayNamesLocale
        }

        fun maxResults(): Int {
            return maxResults
        }

        fun scoreThreshold(): Float {
            return scoreThreshold
        }

        fun categoryAllowlist(): List<String>? {
            return categoryAllowlist
        }

        fun categoryDenylist(): List<String>? {
            return categoryDenylist
        }

        fun runningMode(): RunningMode {
            return runningMode
        }
    }

    public class ImageEmbedderSettingsInternal @JvmOverloads constructor(
        private val l2Normalize: Boolean = DEFAULT_L2_NORMALIZE,
        private val quantize: Boolean = DEFAULT_QUANTIZE,
        private val runningMode: RunningMode = DEFAULT_RUNNING_MODE
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
    }

    public class ObjectDetectorSettingsInternal @JvmOverloads constructor(
        private val displayNamesLocale: String? = DEFAULT_DISPLAY_NAMES_LOCALE,
        private val maxResults: Int = UNLIMITED_RESULTS,
        private val scoreThreshold: Float = DEFAULT_CONFIDENCE,
        private val categoryAllowlist: List<String>? = null,
        private val categoryDenylist: List<String>? = null,
        private val runningMode: RunningMode = DEFAULT_RUNNING_MODE
    ) {
        fun displayNamesLocale(): String? {
            return displayNamesLocale
        }

        fun maxResults(): Int {
            return maxResults
        }

        fun scoreThreshold(): Float {
            return scoreThreshold
        }

        fun categoryAllowlist(): List<String>? {
            return categoryAllowlist
        }

        fun categoryDenylist(): List<String>? {
            return categoryDenylist
        }

        fun runningMode(): RunningMode {
            return runningMode
        }
    }

    public class PoseLandmarkerSettingsInternal @JvmOverloads constructor(
        private val minPoseDetectionConfidence: Float = DEFAULT_CONFIDENCE,
        private val minPosePresenceConfidence: Float = DEFAULT_CONFIDENCE,
        private val minTrackingConfidence: Float = DEFAULT_CONFIDENCE,
        private val numPoses: Int = DEFAULT_NUM_RESULTS,
        private val outputSegmentationMasks: Boolean = DEFAULT_OUTPUT_SEGMENTATION_MASKS,
        private val runningMode: RunningMode = DEFAULT_RUNNING_MODE
    ) {
        fun minPoseDetectionConfidence(): Float {
            return minPoseDetectionConfidence
        }

        fun minPosePresenceConfidence(): Float {
            return minPosePresenceConfidence
        }

        fun minTrackingConfidence(): Float {
            return minTrackingConfidence
        }

        fun numPoses(): Int {
            return numPoses
        }

        fun outputSegmentationMasks(): Boolean {
            return outputSegmentationMasks
        }

        fun runningMode(): RunningMode {
            return runningMode
        }
    }

    public class InteractiveSegmenterSettingsInternal @JvmOverloads constructor(
        private val outputConfidenceMasks: Boolean = DEFAULT_OUTPUT_CONFIDENCE_MASKS,
        private val outputCategoryMask: Boolean = DEFAULT_OUTPUT_CATEGORY_MASK,
        private val runningMode: RunningMode = DEFAULT_RUNNING_MODE
    ) {
        fun outputConfidenceMasks(): Boolean {
            return outputConfidenceMasks
        }

        fun outputCategoryMask(): Boolean {
            return outputCategoryMask
        }

        fun runningMode(): RunningMode {
            return runningMode
        }
    }

    public class ImageSegmenterSettingsInternal @JvmOverloads constructor(
        private val outputConfidenceMasks: Boolean = DEFAULT_OUTPUT_CONFIDENCE_MASKS,
        private val outputCategoryMask: Boolean = DEFAULT_OUTPUT_CATEGORY_MASK,
        private val displayNamesLocale: String? = DEFAULT_DISPLAY_NAMES_LOCALE,
        private val runningMode: RunningMode = DEFAULT_RUNNING_MODE
    ) {
        fun outputConfidenceMasks(): Boolean {
            return outputConfidenceMasks
        }

        fun outputCategoryMask(): Boolean {
            return outputCategoryMask
        }

        fun displayNamesLocale(): String? {
            return displayNamesLocale
        }

        fun runningMode(): RunningMode {
            return runningMode
        }
    }

    /**
     * Removes a previously added model download listener.
     *
     * @param listener The listener to remove.
     */
    fun removeModelDownloadListener(
        model: VisionModel, listener: DownloadListener
    ) {
        throw UnsupportedOperationException()
    }

    /**
     * Adds a listener for NPU delegate initialization events.
     *
     * @param listener The listener to add.
     */
    fun addNpuDelegateDownloadListener(listener: DownloadListener) {
        throw UnsupportedOperationException()
    }

    /**
     * Removes a previously added NPU delegate initialization listener.
     *
     * @param listener The listener to remove.
     */
    fun removeNpuDelegateDownloadsListener(listener: DownloadListener) {
        throw UnsupportedOperationException()
    }

    fun createFaceDetectorImpl(
        model: VisionModel, settings: FaceDetectorSettingsInternal
    ): Future<FaceDetector> {
        throw UnsupportedOperationException()
    }

    fun createFaceLandmarkerImpl(
        model: VisionModel, settings: FaceLandmarkerSettingsInternal
    ): Future<FaceLandmarker>{
        throw UnsupportedOperationException()
    }

    fun createGestureRecognizerImpl(
        model: VisionModel, settings: GestureRecognizerSettingsInternal
    ): Future<GestureRecognizer> {
        throw UnsupportedOperationException()
    }

    fun createHandLandmarkerImpl(
        model: VisionModel, settings: HandLandmarkerSettingsInternal
    ): Future<HandLandmarker> {
        throw UnsupportedOperationException()
    }

    fun createImageClassifierImpl(
        model: VisionModel, settings: ImageClassifierSettingsInternal
    ): Future<ImageClassifier> {
        throw UnsupportedOperationException()
    }

    fun createImageEmbedderImpl(
        model: VisionModel, settings: ImageEmbedderSettingsInternal
    ): Future<ImageEmbedder> {
        throw UnsupportedOperationException()
    }

    fun createImageSegmenterImpl(
        model: VisionModel, settings: ImageSegmenterSettingsInternal
    ): Future<ImageSegmenter> {
        throw UnsupportedOperationException()
    }

    fun createInteractiveSegmenterImpl(
        model: VisionModel, settings: InteractiveSegmenterSettingsInternal
    ): Future<InteractiveSegmenter> {
        throw UnsupportedOperationException()
    }

    fun createObjectDetectorImpl(
        model: VisionModel, settings: ObjectDetectorSettingsInternal
    ): Future<ObjectDetector> {
        throw UnsupportedOperationException()
    }

    fun createPoseLandmarkerImpl(
        model: VisionModel, settings: PoseLandmarkerSettingsInternal
    ): Future<PoseLandmarker> {
        throw UnsupportedOperationException()
    }

    companion object {
        // Reusable Constants
        const val DEFAULT_CONFIDENCE: Float = 0.5f
        const val DEFAULT_NUM_RESULTS: Int = 1
        const val UNLIMITED_RESULTS: Int = -1
        const val DEFAULT_OUTPUT_BLENDSHAPES: Boolean = false
        const val DEFAULT_OUTPUT_FACIAL_TRANSFORMATION_MATRIXES: Boolean = false
          val DEFAULT_RUNNING_MODE: RunningMode = RunningMode.IMAGE
        const val DEFAULT_DISPLAY_NAMES_LOCALE: String = "en"
        val DEFAULT_CATEGORY_LIST: List<String> = listOf()
        const val DEFAULT_L2_NORMALIZE: Boolean = false
        const val DEFAULT_QUANTIZE: Boolean = false
        const val DEFAULT_OUTPUT_CONFIDENCE_MASKS: Boolean = false
        const val DEFAULT_OUTPUT_CATEGORY_MASK: Boolean = false
        const val DEFAULT_OUTPUT_SEGMENTATION_MASKS: Boolean = false

        /**
         * Adds a listener for model download events.
         *
         * @param listener The listener to add.
         */
        fun addModelDownloadListener(
            model: VisionModel, listener: DownloadListener
        ) {
            throw UnsupportedOperationException()
        }
    }
}
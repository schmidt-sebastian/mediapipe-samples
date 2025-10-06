package com.google.mediapipe.tasks.vision.provider

import android.app.Activity
import android.content.Context
import android.util.Log
import android.webkit.DownloadListener
import com.google.android.play.core.aipacks.AiPackLocation
import com.google.android.play.core.aipacks.AiPackManager
import com.google.android.play.core.aipacks.AiPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackState
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.mediapipe.tasks.components.processors.ClassifierOptions
import com.google.mediapipe.tasks.core.BaseOptions
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future

open class VisionProviderBase(private val context: Context) {
    private lateinit var aiPackManager: AiPackManager

    init {
        aiPackManager = AiPackManagerFactory.getInstance(context.applicationContext)

        val splitInstallManager: SplitInstallManager = SplitInstallManagerFactory.create(context)

// 2. Get the set of all installed module names
        val installedModules: Set<String> = splitInstallManager.installedModules

// 3. Log the installed modules
        if (installedModules.isEmpty()) {
            Log.d("AiPackInfo", "No feature modules are currently installed.")
        } else {
            Log.d("AiPackInfo", "Installed modules:")
            installedModules.forEach { moduleName ->
                Log.d("AiPackInfo", "- $moduleName")
            }
        }
    }

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

    private fun getAbsoluteAiAssetPath(aiPack: String, relativeAiAssetPath: String): String {
        val aiPackPath: AiPackLocation? = aiPackManager.getPackLocation(aiPack)

        if (aiPackPath == null) {
            // AI pack is not ready
            // should return null
            throw RuntimeException("AI pack not found " + aiPack)
        }

        val aiAssetsFolderPath: String? = aiPackPath.assetsPath()
        // equivalent to: FilenameUtils.concat(aiPackPath.path(), "assets");
        val aiAssetPath: String = "$aiAssetsFolderPath/$relativeAiAssetPath"
        return aiAssetPath
    }
    fun createInteractiveSegmenterImpl(
        model: VisionModel, settings: InteractiveSegmenterSettingsInternal
    ): Future<InteractiveSegmenter> {
        val future = CompletableFuture<InteractiveSegmenter>()

        val downloader = AIPackDownloader(context)
        val packName = "aipack-" + model.enumName.lowercase().replace("_", "-")
        val modelPath = getAbsoluteAiAssetPath(packName, model.createModelFileName())

        downloader.setListener(object : AIPackDownloader.DownloadListener {
            override fun onStatusUpdate(status: DownloadStatus) {
                when (status) {
                    is DownloadStatus.Completed -> {
                        Log.d("VisionProvider", "AI Pack '${packName}' downloaded successfully.")
                        // Once the download is complete, the asset pack is available to the app's
                        // AssetManager. We can now proceed with creating the MediaPipe task.
                        Executors.newSingleThreadExecutor().submit {
                            try {
                                val baseOptionsBuilder = BaseOptions.builder()
                                    .setModelAssetPath(modelPath)
// runnign mode?

                                val optionsBuilder =
                                    InteractiveSegmenter.InteractiveSegmenterOptions.builder()
                                        .setBaseOptions(baseOptionsBuilder.build())
                                        .setOutputConfidenceMasks(settings.outputConfidenceMasks())
                                        .setOutputCategoryMask(settings.outputCategoryMask())

                                val segmenter = InteractiveSegmenter.createFromOptions(context, optionsBuilder.build())
                                future.complete(segmenter)
                            } catch (e: Exception) {
                                future.completeExceptionally(e)
                            } finally {
                                downloader.removeListener()
                            }
                        }
                    }
                    is DownloadStatus.Failed -> {
                        val errorMessage = "Failed to download AI Pack '${packName}' with error code: ${status.errorCode}"
                        Log.e("VisionProvider", errorMessage)
                        future.completeExceptionally(RuntimeException(errorMessage))
                        downloader.removeListener()
                    }
                    is DownloadStatus.Downloading -> {
                        // You can log progress, but the Future doesn't support progress updates.
                        Log.i("VisionProvider", "Downloading '${packName}': ${status.progress}%")
                    }
                    is DownloadStatus.Idle -> {
                        // The downloader is idle, waiting for the download to start.
                    }
                }
            }

            override fun onShowConfirmationDialog(activity: Activity, status: AssetPackState) {
                // This provider class cannot show a UI dialog.
                // We fail the future and let the calling UI layer handle the user confirmation.
                val errorMessage = "User confirmation required to download '${packName}'. The UI must handle this."
                Log.w("VisionProvider", errorMessage)
                future.completeExceptionally(IllegalStateException(errorMessage))
                downloader.removeListener()
            }
        })

        // Start the download process.
        Log.d("VisionProvider", "Requesting download for AI Pack: '${packName}'")
        downloader.downloadPack(packName)

        return future
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
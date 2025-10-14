package com.google.mediapipe.tasks.vision.provider

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import com.google.android.play.core.aipacks.AiPackManager
import com.google.android.play.core.aipacks.AiPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackState
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.zip.ZipFile
import android.os.Build.SOC_MODEL
import com.google.android.play.core.aipacks.AiPackLocation
import java.io.FileInputStream
import java.nio.channels.FileChannel


open class VisionProviderBase(private val context: Context) {
    private var aiPackManager: AiPackManager =
        AiPackManagerFactory.getInstance(context.applicationContext)
    private var soc: String?

    // A thread-safe map to store listeners for each model.
    private val DownloadListeners = mutableMapOf<VisionModel, MutableList<DownloadListener>>()

    init {
        soc = getHexagonVersionForSoC(SOC_MODEL)
    }

    /**
     * Downloads a Play Feature Delivery module if it's not already installed.
     *
     * @param moduleName The name of the feature module to download.
     * @return A CompletableFuture that completes when the module is installed or fails.
     */
    private fun downloadNpuModuleIfNeeded(soc: String?): CompletableFuture<String?> {
        if (soc == null) {
            return CompletableFuture.completedFuture(null)
        }
        val moduleName = "npu-module-$soc"

        val future = CompletableFuture<String?>()
        val splitInstallManager: SplitInstallManager = SplitInstallManagerFactory.create(context)

        if (splitInstallManager.installedModules.contains(moduleName)) {
            Log.d("VisionProvider", "NPU module '$moduleName' is already installed.")
            // The correct way to get the path to native libraries.
            val nativeLibraryDir = context.applicationInfo.nativeLibraryDir
            Log.d("VisionProvider", "Native library directory: $nativeLibraryDir")
            future.complete(nativeLibraryDir)
            return future
        }


        Log.d("VisionProvider", "Requesting download for NPU module: '$moduleName'")
        val request = SplitInstallRequest.newBuilder().addModule(moduleName).build()

        val listener = SplitInstallStateUpdatedListener { state ->
            when (state.status()) {
                SplitInstallSessionStatus.INSTALLED -> {
                    Log.d("VisionProvider", "NPU module '$moduleName' installed successfully.")
                    // Get the path to the dispatch library from the installed module.
                    val nativeLibraryDir = context.applicationInfo.nativeLibraryDir
                    Log.d("VisionProvider", "NPU module path: $nativeLibraryDir")
                    future.complete(nativeLibraryDir)
                }

                SplitInstallSessionStatus.FAILED -> {
                    val errorMessage =
                        "Failed to download NPU module '$moduleName' with error code: ${state.errorCode()}"
                    Log.e("VisionProvider", errorMessage)
                    future.complete(null)
                }

                SplitInstallSessionStatus.DOWNLOADING -> {
                    val progress =
                        (state.bytesDownloaded() * 100 / state.totalBytesToDownload()).toInt()
                    Log.i("VisionProvider", "Downloading '$moduleName': $progress%")
                }

                else -> {
                    // Log other states for debugging if necessary
                    Log.d("VisionProvider", "NPU module download status: ${state.status()}")
                }
            }
        }

        splitInstallManager.registerListener(listener)
        splitInstallManager.startInstall(request).addOnFailureListener { e ->
            future.completeExceptionally(e)
        }

        // Clean up the listener once the operation is complete
        future.whenComplete { _, _ -> splitInstallManager.unregisterListener(listener) }

        return future
    }

    fun getHexagonVersionForSoC(socIdentifier: String): String? {
        return when {
//            // Snapdragon 8 Gen 3
//            socIdentifier.contains("SM8650", ignoreCase = true) -> "v75"
//
//            // Snapdragon 8 Gen 2
//            socIdentifier.contains("SM8550", ignoreCase = true) -> "v73"
//
//            // Snapdragon 8 Gen 1 / 8+ Gen 1
//            socIdentifier.contains("SM8450", ignoreCase = true) ||
//                    socIdentifier.contains("SM8475", ignoreCase = true) -> "v69"
//
//            // Snapdragon 7 series
//            socIdentifier.contains("SM7325", ignoreCase = true) -> "v69" // Snapdragon 778G
//            socIdentifier.contains("SM7450", ignoreCase = true) -> "v69" // Snapdragon 7 Gen 1
//
//            // Snapdragon 888 / 888+
//            socIdentifier.contains("SM8350", ignoreCase = true) -> "v68"

            else -> return null// Return null if the SoC is not in our list
        }
    }

    /**
     * Adds a listener for model download events.
     *
     * @param model The vision model to listen for.
     * @param listener The listener to add.
     */
    @Synchronized
    fun addDownloadListener(
        model: VisionModel, listener: DownloadListener
    ) {
        val listeners = DownloadListeners.getOrPut(model) { mutableListOf() }
        listeners.add(listener)
    }

    /**
     * Removes a previously added model download listener.
     *
     * @param model The vision model the listener is registered to.
     * @param listener The listener to remove.
     */
    @Synchronized
    fun removeDownloadListener(
        model: VisionModel, listener: DownloadListener
    ) {
        DownloadListeners[model]?.let {
            it.remove(listener)
            if (it.isEmpty()) {
                DownloadListeners.remove(model)
            }
        }
    }

    /**
     * Helper function to notify all registered listeners for a specific model.
     */
    private fun notifyModelListeners(model: VisionModel, action: (DownloadListener) -> Unit) {
        synchronized(this) {
            DownloadListeners[model]?.forEach(action)
        }
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

    private fun getAbsoluteAiAssetPath(
        packLocation: AiPackLocation,
        relativeAiAssetPath: String
    ): String {
        return "${packLocation.assetsPath()}/$relativeAiAssetPath"
    }

    /**
     * Generic helper to create a BaseOptions builder with a model path and an optional NPU delegate.
     */
    private fun createBaseOptions(
        modelAssetPath: String?,
        assetFd: AssetFileDescriptor?,
        dispatchLibraryPath: String?
    ): BaseOptions.Builder {
        val baseOptionsBuilder = BaseOptions.builder()

        if (modelAssetPath != null) {
            baseOptionsBuilder.setModelAssetPath(modelAssetPath)
        } else if (assetFd != null) {
            FileInputStream(assetFd.fileDescriptor).use { inputStream ->
                // Memory-map the file channel to get a direct ByteBuffer
                val modelBuffer = inputStream.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    assetFd.startOffset,
                    assetFd.declaredLength
                )
                baseOptionsBuilder.setModelAssetBuffer(modelBuffer)
            }
        }

        // Apply the NPU delegate if a dispatch library path is available
        if (dispatchLibraryPath != null) {
            val npuOptions = BaseOptions.DelegateOptions.NpuOptions.builder()
                .setDispatchLibraryDirectory(dispatchLibraryPath)
                .build()
            baseOptionsBuilder.setDelegate(Delegate.NPU).setDelegateOptions(npuOptions)
        }
        return baseOptionsBuilder
    }

    private fun createFaceDetectorFromPath(
        context: Context,
        modelAssetPath: String?,
        modelAssetFd: AssetFileDescriptor?,
        dispatchLibraryPath: String?,
        settings: FaceDetectorSettingsInternal
    ): FaceDetector {
        val baseOptions =
            createBaseOptions(modelAssetPath, modelAssetFd, dispatchLibraryPath).build()
        val optionsBuilder = FaceDetector.FaceDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(settings.runningMode())
            .setMinDetectionConfidence(settings.minDetectionConfidence())
            .setMinSuppressionThreshold(settings.minSuppressionThreshold())
        settings.resultListener()?.let {
            optionsBuilder.setResultListener(it)
        }
        settings.errorListener()?.let {
            optionsBuilder.setErrorListener(it)
        }
        return FaceDetector.createFromOptions(context, optionsBuilder.build())
    }

    private fun createFaceLandmarkerFromPath(
        context: Context,
        modelAssetPath: String?,
        modelAssetFd: AssetFileDescriptor?,
        dispatchLibraryPath: String?,
        settings: FaceLandmarkerSettingsInternal
    ): FaceLandmarker {
        val baseOptions =
            createBaseOptions(modelAssetPath, modelAssetFd, dispatchLibraryPath).build()
        val optionsBuilder = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(settings.runningMode())
            .setNumFaces(settings.numFaces())
            .setMinFaceDetectionConfidence(settings.minFaceDetectionConfidence())
            .setMinFacePresenceConfidence(settings.minFacePresenceConfidence())
            .setMinTrackingConfidence(settings.minTrackingConfidence())
            .setOutputFaceBlendshapes(settings.outputFaceBlendshapes())
            .setOutputFacialTransformationMatrixes(settings.outputFacialTransformationMatrixes())
        settings.resultListener()?.let {
            optionsBuilder.setResultListener(it)
        }
        settings.errorListener()?.let {
            optionsBuilder.setErrorListener(it)
        }
        return FaceLandmarker.createFromOptions(context, optionsBuilder.build())
    }

    private fun createGestureRecognizerFromPath(
        context: Context,
        modelAssetPath: String?,
        modelAssetFd: AssetFileDescriptor?,
        dispatchLibraryPath: String?,
        settings: GestureRecognizerSettingsInternal
    ): GestureRecognizer {
        val baseOptions =
            createBaseOptions(modelAssetPath, modelAssetFd, dispatchLibraryPath).build()
        val optionsBuilder = GestureRecognizer.GestureRecognizerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(settings.runningMode())
            .setNumHands(settings.numHands())
            .setMinHandDetectionConfidence(settings.minHandDetectionConfidence())
            .setMinHandPresenceConfidence(settings.minHandPresenceConfidence())
            .setMinTrackingConfidence(settings.minTrackingConfidence())
        settings.resultListener()?.let {
            optionsBuilder.setResultListener(it)
        }
        settings.errorListener()?.let {
            optionsBuilder.setErrorListener(it)
        }
        return GestureRecognizer.createFromOptions(context, optionsBuilder.build())
    }

    private fun createHandLandmarkerFromPath(
        context: Context,
        modelAssetPath: String?,
        modelAssetFd: AssetFileDescriptor?,
        dispatchLibraryPath: String?,
        settings: HandLandmarkerSettingsInternal
    ): HandLandmarker {
        val baseOptions =
            createBaseOptions(modelAssetPath, modelAssetFd, dispatchLibraryPath).build()
        val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(settings.runningMode())
            .setNumHands(settings.numHands())
            .setMinHandDetectionConfidence(settings.minHandDetectionConfidence())
            .setMinHandPresenceConfidence(settings.minHandPresenceConfidence())
            .setMinTrackingConfidence(settings.minTrackingConfidence())
        settings.resultListener()?.let {
            optionsBuilder.setResultListener(it)
        }
        settings.errorListener()?.let {
            optionsBuilder.setErrorListener(it)
        }
        return HandLandmarker.createFromOptions(context, optionsBuilder.build())
    }

    private fun createImageClassifierFromPath(
        context: Context,
        modelAssetPath: String?,
        modelAssetFd: AssetFileDescriptor?,
        dispatchLibraryPath: String?,
        settings: ImageClassifierSettingsInternal
    ): ImageClassifier {
        val baseOptions =
            createBaseOptions(modelAssetPath, modelAssetFd, dispatchLibraryPath).build()
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(settings.runningMode())
            .setDisplayNamesLocale(settings.displayNamesLocale())
            .setMaxResults(settings.maxResults())
            .setScoreThreshold(settings.scoreThreshold())
            .setCategoryAllowlist(settings.categoryAllowlist())
            .setCategoryDenylist(settings.categoryDenylist())
        settings.resultListener()?.let {
            optionsBuilder.setResultListener(it)
        }
        settings.errorListener()?.let {
            optionsBuilder.setErrorListener(it)
        }
        return ImageClassifier.createFromOptions(context, optionsBuilder.build())
    }

    private fun createImageEmbedderFromPath(
        context: Context,
        modelAssetPath: String?,
        modelAssetFd: AssetFileDescriptor?,
        dispatchLibraryPath: String?,
        settings: ImageEmbedderSettingsInternal
    ): ImageEmbedder {
        val baseOptions =
            createBaseOptions(modelAssetPath, modelAssetFd, dispatchLibraryPath).build()
        val optionsBuilder = ImageEmbedder.ImageEmbedderOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(settings.runningMode())
            .setL2Normalize(settings.l2Normalize())
            .setQuantize(settings.quantize())
        settings.resultListener()?.let {
            optionsBuilder.setResultListener(it)
        }
        settings.errorListener()?.let {
            optionsBuilder.setErrorListener(it)
        }
        return ImageEmbedder.createFromOptions(context, optionsBuilder.build())
    }

    private fun createImageSegmenterFromPath(
        context: Context,
        modelAssetPath: String?,
        modelAssetFd: AssetFileDescriptor?,
        dispatchLibraryPath: String?,
        settings: ImageSegmenterSettingsInternal
    ): ImageSegmenter {
        val baseOptions =
            createBaseOptions(modelAssetPath, modelAssetFd, dispatchLibraryPath).build()
        val optionsBuilder = ImageSegmenter.ImageSegmenterOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(settings.runningMode())
            .setOutputConfidenceMasks(settings.outputConfidenceMasks())
            .setOutputCategoryMask(settings.outputCategoryMask())
        settings.resultListener()?.let {
            optionsBuilder.setResultListener(it)
        }
        settings.errorListener()?.let {
            optionsBuilder.setErrorListener(it)
        }
        return ImageSegmenter.createFromOptions(context, optionsBuilder.build())
    }

    private fun createInteractiveSegmenterFromPath(
        context: Context,
        modelAssetPath: String?,
        modelAssetFd: AssetFileDescriptor?,
        dispatchLibraryPath: String?,
        settings: InteractiveSegmenterSettingsInternal
    ): InteractiveSegmenter {
        val baseOptions =
            createBaseOptions(modelAssetPath, modelAssetFd, dispatchLibraryPath).build()
        val optionsBuilder = InteractiveSegmenter.InteractiveSegmenterOptions.builder()
            .setBaseOptions(baseOptions)
            .setOutputConfidenceMasks(settings.outputConfidenceMasks())
            .setOutputCategoryMask(settings.outputCategoryMask())
        settings.resultListener()?.let {
            optionsBuilder.setResultListener(it)
        }
        settings.errorListener()?.let {
            optionsBuilder.setErrorListener(it)
        }
        return InteractiveSegmenter.createFromOptions(context, optionsBuilder.build())
    }

    private fun createObjectDetectorFromPath(
        context: Context,
        modelAssetPath: String?,
        modelAssetFd: AssetFileDescriptor?,
        dispatchLibraryPath: String?,
        settings: ObjectDetectorSettingsInternal
    ): ObjectDetector {
        val baseOptions =
            createBaseOptions(modelAssetPath, modelAssetFd, dispatchLibraryPath).build()
        val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(settings.runningMode())
            .setDisplayNamesLocale(settings.displayNamesLocale())
            .setMaxResults(settings.maxResults())
            .setScoreThreshold(settings.scoreThreshold())
            .setCategoryAllowlist(settings.categoryAllowlist())
            .setCategoryDenylist(settings.categoryDenylist())
        settings.resultListener()?.let {
            optionsBuilder.setResultListener(it)
        }
        settings.errorListener()?.let {
            optionsBuilder.setErrorListener(it)
        }
        return ObjectDetector.createFromOptions(context, optionsBuilder.build())
    }

    private fun createPoseLandmarkerFromPath(
        context: Context,
        modelAssetPath: String?,
        modelAssetFd: AssetFileDescriptor?,
        dispatchLibraryPath: String?,
        settings: PoseLandmarkerSettingsInternal
    ): PoseLandmarker {
        val baseOptions =
            createBaseOptions(modelAssetPath, modelAssetFd, dispatchLibraryPath).build()
        val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(settings.runningMode())
            .setNumPoses(settings.numPoses())
            .setMinPoseDetectionConfidence(settings.minPoseDetectionConfidence())
            .setMinPosePresenceConfidence(settings.minPosePresenceConfidence())
            .setMinTrackingConfidence(settings.minTrackingConfidence())
            .setOutputSegmentationMasks(settings.outputSegmentationMasks())
        settings.resultListener()?.let {
            optionsBuilder.setResultListener(it)
        }
        settings.errorListener()?.let {
            optionsBuilder.setErrorListener(it)
        }
        return PoseLandmarker.createFromOptions(context, optionsBuilder.build())
    }

    /**
     * Generic function to create any MediaPipe Vision task.
     * It handles NPU delegate setup, AI pack downloading, and task instantiation.
     */
    private fun <T, S> createTask(
        model: VisionModel,
        settings: S,
        creator: (Context, String?, AssetFileDescriptor?, String?, S) -> T
    ): Future<T> {
        val future = CompletableFuture<T>()

        downloadNpuModuleIfNeeded(soc).whenComplete { dispatchLibraryPath, npuException ->
            if (npuException != null) {
                Log.e("VisionProvider", "Failed to prepare NPU module.", npuException)
                future.completeExceptionally(npuException)
                return@whenComplete
            }

            val packName = "aipack_" + model.enumName
            try {
                val packLocation = aiPackManager.getPackLocation(packName)
                if (packLocation != null) {
                    Log.d(
                        "VisionProvider",
                        "AI Pack '$packName' is already installed. Creating task."
                    )
                    val filename = model.createModelFileName()
                    Executors.newSingleThreadExecutor().submit {
                        try {
                            if (packLocation.assetsPath() != null) {
                                val modelPath =
                                    getAbsoluteAiAssetPath(packLocation, filename)
                                val task =
                                    creator(context, modelPath, null, dispatchLibraryPath, settings)
                                future.complete(task)
                            } else {
                                val fd = context.assets.openFd("model/" + filename)
                                val task =
                                    creator(context, null, fd, dispatchLibraryPath, settings)
                                future.complete(task)
                            }
                        } catch (e: Exception) {
                            future.completeExceptionally(e)
                        }
                    }
                } else {
                    Log.d("VisionProvider", "AI Pack '$packName' not found. Starting download.")
                    initiateAIPackDownloadForTask(
                        future,
                        packName,
                        model,
                        dispatchLibraryPath,
                        settings,
                        creator
                    )
                }
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    /**
     * Initiates the download process for a given AI Pack and creates the task on completion.
     */
    private fun <T, S> initiateAIPackDownloadForTask(
        future: CompletableFuture<T>,
        packName: String,
        model: VisionModel,
        dispatchLibraryPath: String?,
        settings: S,
        creator: (Context, String?, AssetFileDescriptor?, String?, S) -> T
    ) {
        val downloader = AIPackDownloader(context)
        downloader.setListener(object : AIPackDownloader.DownloadListener {
            override fun onStatusUpdate(status: DownloadStatus) {
                when (status) {
                    is DownloadStatus.Completed -> {
                        Log.d("VisionProvider", "AI Pack '$packName' downloaded. Creating task.")
                        notifyModelListeners(model) { it.onCompleted() }
                        val packLocation = aiPackManager.getPackLocation(packName)

                        Executors.newSingleThreadExecutor().submit {
                            try {
                                if (packLocation == null || packLocation.assetsPath() == null) {
                                    val fd = context.assets.openFd(model.createModelFileName())
                                    val task =
                                        creator(context, null, fd, dispatchLibraryPath, settings)
                                    future.complete(task)
                                } else {
                                    val modelPath =
                                        getAbsoluteAiAssetPath(
                                            packLocation,
                                            model.createModelFileName()
                                        )
                                    val task =
                                        creator(
                                            context,
                                            modelPath,
                                            null,
                                            dispatchLibraryPath,
                                            settings
                                        )
                                    future.complete(task)
                                }

                            } catch (e: Exception) {
                                future.completeExceptionally(e)
                            } finally {
                                downloader.removeListener()
                            }
                        }
                    }

                    is DownloadStatus.Failed -> {
                        val error =
                            RuntimeException("Failed to download AI Pack '$packName' with error: ${status.errorCode}")
                        Log.e("VisionProvider", error.message!!)
                        notifyModelListeners(model) { it.onFailed(error) }
                        future.completeExceptionally(error)
                        downloader.removeListener()
                    }

                    is DownloadStatus.Downloading -> {
                        Log.i("VisionProvider", "Downloading '$packName': ${status.progress}%")
                        notifyModelListeners(model) { it.onProgress(status.progress) }
                    }

                    is DownloadStatus.Idle -> { /* Idle state, no action needed. */
                    }
                }
            }

            override fun onShowConfirmationDialog(activity: Activity, status: AssetPackState) {
                val error =
                    IllegalStateException("User confirmation required for '$packName'. The UI must handle this.")
                Log.w("VisionProvider", error.message!!)
                notifyModelListeners(model) { it.onFailed(error) }
                future.completeExceptionally(error)
                downloader.removeListener()
            }
        })
        downloader.downloadPack(packName)
    }

    // ==================== Public Impl Functions ====================

    fun createFaceDetectorImpl(
        model: VisionModel, settings: FaceDetectorSettingsInternal
    ): Future<FaceDetector> = createTask(model, settings, ::createFaceDetectorFromPath)

    fun createFaceLandmarkerImpl(
        model: VisionModel, settings: FaceLandmarkerSettingsInternal
    ): Future<FaceLandmarker> = createTask(model, settings, ::createFaceLandmarkerFromPath)

    fun createGestureRecognizerImpl(
        model: VisionModel, settings: GestureRecognizerSettingsInternal
    ): Future<GestureRecognizer> = createTask(model, settings, ::createGestureRecognizerFromPath)

    fun createHandLandmarkerImpl(
        model: VisionModel, settings: HandLandmarkerSettingsInternal
    ): Future<HandLandmarker> = createTask(model, settings, ::createHandLandmarkerFromPath)

    fun createImageClassifierImpl(
        model: VisionModel, settings: ImageClassifierSettingsInternal
    ): Future<ImageClassifier> = createTask(model, settings, ::createImageClassifierFromPath)

    fun createImageEmbedderImpl(
        model: VisionModel, settings: ImageEmbedderSettingsInternal
    ): Future<ImageEmbedder> = createTask(model, settings, ::createImageEmbedderFromPath)

    fun createImageSegmenterImpl(
        model: VisionModel, settings: ImageSegmenterSettingsInternal
    ): Future<ImageSegmenter> = createTask(model, settings, ::createImageSegmenterFromPath)

    fun createInteractiveSegmenterImpl(
        model: VisionModel, settings: InteractiveSegmenterSettingsInternal
    ): Future<InteractiveSegmenter> =
        createTask(model, settings, ::createInteractiveSegmenterFromPath)

    fun createObjectDetectorImpl(
        model: VisionModel, settings: ObjectDetectorSettingsInternal
    ): Future<ObjectDetector> = createTask(model, settings, ::createObjectDetectorFromPath)

    fun createPoseLandmarkerImpl(
        model: VisionModel, settings: PoseLandmarkerSettingsInternal
    ): Future<PoseLandmarker> = createTask(model, settings, ::createPoseLandmarkerFromPath)


    companion object {
        const val UNLIMITED_RESULTS: Int = -1
        const val DEFAULT_OUTPUT_BLENDSHAPES: Boolean = false
        const val DEFAULT_CONFIDENCE = 0.5f
        const val DEFAULT_NUM_RESULTS = -1
        const val DEFAULT_OUTPUT_FACIAL_TRANSFORMATION_MATRIXES: Boolean = false
        val DEFAULT_RUNNING_MODE: RunningMode = RunningMode.IMAGE
        const val DEFAULT_DISPLAY_NAMES_LOCALE: String = "en"
        val DEFAULT_CATEGORY_LIST: List<String> = listOf()
        const val DEFAULT_L2_NORMALIZE: Boolean = false
        const val DEFAULT_QUANTIZE: Boolean = false
        const val DEFAULT_OUTPUT_CONFIDENCE_MASKS: Boolean = false
        const val DEFAULT_OUTPUT_CATEGORY_MASK: Boolean = true
        const val DEFAULT_OUTPUT_SEGMENTATION_MASKS: Boolean = false
    }
}
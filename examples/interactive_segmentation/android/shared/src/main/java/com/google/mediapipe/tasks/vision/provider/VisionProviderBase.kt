package com.google.mediapipe.tasks.vision.provider

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.os.Build.SOC_MODEL
import android.util.Log
import com.google.android.play.core.aipacks.AiPackLocation
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
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future


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
     * Unified method to load the model file into a ByteBuffer.
     * It prioritizes the downloaded AI Pack and falls back to the app's local assets.
     */
    private fun getModelAsBuffer(model: VisionModel): ByteBuffer {
        val packName = "aipack_" + model.enumName
        val filename = model.createModelFileName()
        val packLocation = aiPackManager.getPackLocation(packName)

        // Prioritize downloaded AI Pack if available
        if (packLocation?.assetsPath() != null) {
            val modelPath = getAbsoluteAiAssetPath(packLocation, filename)
            val modelFile = File(modelPath)
            if (modelFile.exists()) {
                return FileInputStream(modelFile).use { inputStream ->
                    inputStream.channel.map(
                        FileChannel.MapMode.READ_ONLY, 0, modelFile.length()
                    )
                }
            } else {
                throw FileNotFoundException("Model file not found: " + modelFile)
            }
        }

        // Fallback to model included in the app's assets directory
        return context.assets.openFd("model/$filename").use { afd ->
            FileInputStream(afd.fileDescriptor).use { inputStream ->
                inputStream.channel.map(
                    FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength
                )
            }
        }
    }

    /**
     * Generic helper to create a BaseOptions builder with a model buffer and an optional NPU delegate.
     */
    private fun createBaseOptions(
        modelAssetBuffer: ByteBuffer, dispatchLibraryPath: String?
    ): BaseOptions.Builder {
        val baseOptionsBuilder = BaseOptions.builder().setModelAssetBuffer(modelAssetBuffer)

        // Apply the NPU delegate if a dispatch library path is available
        if (dispatchLibraryPath != null) {
            val npuOptions = BaseOptions.DelegateOptions.NpuOptions.builder()
                .setDispatchLibraryDirectory(dispatchLibraryPath).build()
            baseOptionsBuilder.setDelegate(Delegate.NPU).setDelegateOptions(npuOptions)
        }
        return baseOptionsBuilder
    }

    private fun createFaceDetector(
        context: Context,
        modelAssetBuffer: ByteBuffer,
        dispatchLibraryPath: String?,
        settings: FaceDetectorSettingsInternal
    ): FaceDetector {
        val baseOptions = createBaseOptions(modelAssetBuffer, dispatchLibraryPath).build()
        val optionsBuilder = FaceDetector.FaceDetectorOptions.builder().setBaseOptions(baseOptions)
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

    private fun createFaceLandmarker(
        context: Context,
        modelAssetBuffer: ByteBuffer,
        dispatchLibraryPath: String?,
        settings: FaceLandmarkerSettingsInternal
    ): FaceLandmarker {
        val baseOptions = createBaseOptions(modelAssetBuffer, dispatchLibraryPath).build()
        val optionsBuilder =
            FaceLandmarker.FaceLandmarkerOptions.builder().setBaseOptions(baseOptions)
                .setRunningMode(settings.runningMode()).setNumFaces(settings.numFaces())
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

    private fun createGestureRecognizer(
        context: Context,
        modelAssetBuffer: ByteBuffer,
        dispatchLibraryPath: String?,
        settings: GestureRecognizerSettingsInternal
    ): GestureRecognizer {
        val baseOptions = createBaseOptions(modelAssetBuffer, dispatchLibraryPath).build()
        val optionsBuilder =
            GestureRecognizer.GestureRecognizerOptions.builder().setBaseOptions(baseOptions)
                .setRunningMode(settings.runningMode()).setNumHands(settings.numHands())
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

    private fun createHandLandmarker(
        context: Context,
        modelAssetBuffer: ByteBuffer,
        dispatchLibraryPath: String?,
        settings: HandLandmarkerSettingsInternal
    ): HandLandmarker {
        val baseOptions = createBaseOptions(modelAssetBuffer, dispatchLibraryPath).build()
        val optionsBuilder =
            HandLandmarker.HandLandmarkerOptions.builder().setBaseOptions(baseOptions)
                .setRunningMode(settings.runningMode()).setNumHands(settings.numHands())
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

    private fun createImageClassifier(
        context: Context,
        modelAssetBuffer: ByteBuffer,
        dispatchLibraryPath: String?,
        settings: ImageClassifierSettingsInternal
    ): ImageClassifier {
        val baseOptions = createBaseOptions(modelAssetBuffer, dispatchLibraryPath).build()
        val optionsBuilder =
            ImageClassifier.ImageClassifierOptions.builder().setBaseOptions(baseOptions)
                .setRunningMode(settings.runningMode())
                .setDisplayNamesLocale(settings.displayNamesLocale())
                .setMaxResults(settings.maxResults()).setScoreThreshold(settings.scoreThreshold())
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

    private fun createImageEmbedder(
        context: Context,
        modelAssetBuffer: ByteBuffer,
        dispatchLibraryPath: String?,
        settings: ImageEmbedderSettingsInternal
    ): ImageEmbedder {
        val baseOptions = createBaseOptions(modelAssetBuffer, dispatchLibraryPath).build()
        val optionsBuilder =
            ImageEmbedder.ImageEmbedderOptions.builder().setBaseOptions(baseOptions)
                .setRunningMode(settings.runningMode()).setL2Normalize(settings.l2Normalize())
                .setQuantize(settings.quantize())
        settings.resultListener()?.let {
            optionsBuilder.setResultListener(it)
        }
        settings.errorListener()?.let {
            optionsBuilder.setErrorListener(it)
        }
        return ImageEmbedder.createFromOptions(context, optionsBuilder.build())
    }

    private fun createImageSegmenter(
        context: Context,
        modelAssetBuffer: ByteBuffer,
        dispatchLibraryPath: String?,
        settings: ImageSegmenterSettingsInternal
    ): ImageSegmenter {
        val baseOptions = createBaseOptions(modelAssetBuffer, dispatchLibraryPath).build()
        val optionsBuilder =
            ImageSegmenter.ImageSegmenterOptions.builder().setBaseOptions(baseOptions)
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

    private fun createInteractiveSegmenter(
        context: Context,
        modelAssetBuffer: ByteBuffer,
        dispatchLibraryPath: String?,
        settings: InteractiveSegmenterSettingsInternal
    ): InteractiveSegmenter {
        val baseOptions = createBaseOptions(modelAssetBuffer, dispatchLibraryPath).build()
        val optionsBuilder =
            InteractiveSegmenter.InteractiveSegmenterOptions.builder().setBaseOptions(baseOptions)
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

    private fun createObjectDetector(
        context: Context,
        modelAssetBuffer: ByteBuffer,
        dispatchLibraryPath: String?,
        settings: ObjectDetectorSettingsInternal
    ): ObjectDetector {
        val baseOptions = createBaseOptions(modelAssetBuffer, dispatchLibraryPath).build()
        val optionsBuilder =
            ObjectDetector.ObjectDetectorOptions.builder().setBaseOptions(baseOptions)
                .setRunningMode(settings.runningMode())
                .setDisplayNamesLocale(settings.displayNamesLocale())
                .setMaxResults(settings.maxResults()).setScoreThreshold(settings.scoreThreshold())
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

    private fun createPoseLandmarker(
        context: Context,
        modelAssetBuffer: ByteBuffer,
        dispatchLibraryPath: String?,
        settings: PoseLandmarkerSettingsInternal
    ): PoseLandmarker {
        val baseOptions = createBaseOptions(modelAssetBuffer, dispatchLibraryPath).build()
        val optionsBuilder =
            PoseLandmarker.PoseLandmarkerOptions.builder().setBaseOptions(baseOptions)
                .setRunningMode(settings.runningMode()).setNumPoses(settings.numPoses())
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
        creator: (Context, ByteBuffer, String?, S) -> T
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
                if (aiPackManager.getPackLocation(packName) != null) {
                    Log.d(
                        "VisionProvider",
                        "AI Pack '$packName' is already installed. Creating task."
                    )
                    Executors.newSingleThreadExecutor().submit {
                        try {
                            val modelBuffer = getModelAsBuffer(model)
                            val task =
                                creator(context, modelBuffer, dispatchLibraryPath, settings)
                            future.complete(task)
                        } catch (e: Exception) {
                            future.completeExceptionally(e)
                        }
                    }
                } else {
                    Log.d("VisionProvider", "AI Pack '$packName' not found. Starting download.")
                    initiateAIPackDownloadForTask(
                        future, packName, model, dispatchLibraryPath, settings, creator
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
        creator: (Context, ByteBuffer, String?, S) -> T
    ) {
        val downloader = AIPackDownloader(context)
        downloader.setListener(object : AIPackDownloader.DownloadListener {
            override fun onStatusUpdate(status: DownloadStatus) {
                when (status) {
                    is DownloadStatus.Completed -> {
                        Log.d("VisionProvider", "AI Pack '$packName' downloaded. Creating task.")
                        notifyModelListeners(model) { it.onCompleted() }

                        Executors.newSingleThreadExecutor().submit {
                            try {
                                val modelBuffer = getModelAsBuffer(model)
                                val task =
                                    creator(context, modelBuffer, dispatchLibraryPath, settings)
                                future.complete(task)
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
    ): Future<FaceDetector> = createTask(model, settings, ::createFaceDetector)

    fun createFaceLandmarkerImpl(
        model: VisionModel, settings: FaceLandmarkerSettingsInternal
    ): Future<FaceLandmarker> = createTask(model, settings, ::createFaceLandmarker)

    fun createGestureRecognizerImpl(
        model: VisionModel, settings: GestureRecognizerSettingsInternal
    ): Future<GestureRecognizer> = createTask(model, settings, ::createGestureRecognizer)

    fun createHandLandmarkerImpl(
        model: VisionModel, settings: HandLandmarkerSettingsInternal
    ): Future<HandLandmarker> = createTask(model, settings, ::createHandLandmarker)

    fun createImageClassifierImpl(
        model: VisionModel, settings: ImageClassifierSettingsInternal
    ): Future<ImageClassifier> = createTask(model, settings, ::createImageClassifier)

    fun createImageEmbedderImpl(
        model: VisionModel, settings: ImageEmbedderSettingsInternal
    ): Future<ImageEmbedder> = createTask(model, settings, ::createImageEmbedder)

    fun createImageSegmenterImpl(
        model: VisionModel, settings: ImageSegmenterSettingsInternal
    ): Future<ImageSegmenter> = createTask(model, settings, ::createImageSegmenter)

    fun createInteractiveSegmenterImpl(
        model: VisionModel, settings: InteractiveSegmenterSettingsInternal
    ): Future<InteractiveSegmenter> = createTask(model, settings, ::createInteractiveSegmenter)

    fun createObjectDetectorImpl(
        model: VisionModel, settings: ObjectDetectorSettingsInternal
    ): Future<ObjectDetector> = createTask(model, settings, ::createObjectDetector)

    fun createPoseLandmarkerImpl(
        model: VisionModel, settings: PoseLandmarkerSettingsInternal
    ): Future<PoseLandmarker> = createTask(model, settings, ::createPoseLandmarker)


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
package com.google.mediapipe.tasks.vision.provider

import android.app.Activity
import android.content.Context
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


open class VisionProviderBase(private val context: Context) {
    private var aiPackManager: AiPackManager = AiPackManagerFactory.getInstance(context.applicationContext)
    private var soc: String?

    // A thread-safe map to store listeners for each model.
    private val DownloadListeners = mutableMapOf<VisionModel, MutableList<DownloadListener>>()

    init {
        soc = null // getHexagonVersionForSoC(SOC_MODEL)
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
            // Snapdragon 8 Gen 3
            socIdentifier.contains("SM8650", ignoreCase = true) -> "v75"

            // Snapdragon 8 Gen 2
            socIdentifier.contains("SM8550", ignoreCase = true) -> "v73"

            // Snapdragon 8 Gen 1 / 8+ Gen 1
            socIdentifier.contains("SM8450", ignoreCase = true) ||
                    socIdentifier.contains("SM8475", ignoreCase = true) -> "v69"

            // Snapdragon 7 series
            socIdentifier.contains("SM7325", ignoreCase = true) -> "v69" // Snapdragon 778G
            socIdentifier.contains("SM7450", ignoreCase = true) -> "v69" // Snapdragon 7 Gen 1

            // Snapdragon 888 / 888+
            socIdentifier.contains("SM8350", ignoreCase = true) -> "v68"

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

    fun createFaceDetectorImpl(
        model: VisionModel, settings: FaceDetectorSettingsInternal
    ): Future<FaceDetector> {
        throw UnsupportedOperationException()
    }

    fun createFaceLandmarkerImpl(
        model: VisionModel, settings: FaceLandmarkerSettingsInternal
    ): Future<FaceLandmarker> {
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


    // You will need to pass a Context to this function
    private fun getAbsoluteAiAssetPath(
        context: Context,
        aiPack: String,
        relativeAiAssetPath: String
    ): String {
        // First, confirm the pack is registered and available
        val aiPackLocation = aiPackManager.getPackLocation(aiPack)
            ?: throw RuntimeException("AI pack not found or ready: $aiPack")
        // properly load from AI pack

        // Instead of relying on assetsPath(), find and copy the asset to get a stable path
        return getAssetPathFromSplits(context, relativeAiAssetPath)
            ?: throw RuntimeException("Asset '$relativeAiAssetPath' not found in pack '$aiPack' or any other split.")
    }

    /**
     * Finds an asset by searching through the base APK and all installed split APKs.
     * If found, it copies the asset to the app's cache directory and returns the
     * absolute file path. Returns null if the asset is not found.
     *
     * @param context The application context.
     * @param relativeAssetPath The path of the asset relative to the `assets` folder,
     * e.g., "my_model.tflite".
     * @return The absolute path to the cached asset file, or null if not found.
     */
    private fun getAssetPathFromSplits(context: Context, relativeAssetPath: String): String? {
        val outputFile = File(context.cacheDir, relativeAssetPath)

        // If the file is already cached, just return its path
        if (outputFile.exists()) {
            return outputFile.absolutePath
        }

        val assetPathInApk = "assets/$relativeAssetPath"
        val appInfo = context.applicationInfo

        // Create a list of all APKs to search (base + splits)
        val apksToSearch = mutableListOf(appInfo.sourceDir)
        appInfo.splitSourceDirs?.let { apksToSearch.addAll(it) }

        for (apkPath in apksToSearch) {
            try {
                ZipFile(apkPath).use { zip ->
                    val entry = zip.getEntry(assetPathInApk)
                    if (entry != null) {
                        // Asset found, copy it to the cache directory
                        outputFile.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            FileOutputStream(outputFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        // Return the path of the newly cached file
                        return outputFile.absolutePath
                    }
                }
            } catch (e: IOException) {
                // Could happen if a splitSourceDir path is invalid; continue to the next
                Log.e("AssetFinder", "Failed to read APK at $apkPath", e)
            }
        }

        // Return null if the asset was not found in any APK
        return null
    }

    /**
     * Creates an InteractiveSegmenter instance from a given model path and settings.
     * This helper function encapsulates the MediaPipe object creation logic.
     */
    private fun createSegmenterFromPath(
        context: Context,
        modelAssetPath: String,
        dispatchLibraryPath: String?,
        settings: InteractiveSegmenterSettingsInternal
    ): InteractiveSegmenter {
        val baseOptionsBuilder = BaseOptions.builder()
            .setModelAssetPath(modelAssetPath)

        // Apply the NPU delegate if a dispatch library path is available
        if (dispatchLibraryPath != null) {
            val npuOptions = BaseOptions.DelegateOptions.NpuOptions.builder()
                .setDispatchLibraryDirectory(dispatchLibraryPath)
                .build()
            baseOptionsBuilder.setDelegate(Delegate.NPU).setDelegateOptions(npuOptions)
        }

        val optionsBuilder = InteractiveSegmenter.InteractiveSegmenterOptions.builder()
            .setBaseOptions(baseOptionsBuilder.build())
            .setOutputConfidenceMasks(settings.outputConfidenceMasks())
            .setOutputCategoryMask(settings.outputCategoryMask())

        return InteractiveSegmenter.createFromOptions(context, optionsBuilder.build())
    }


    fun createInteractiveSegmenterImpl(
        model: VisionModel,
        settings: InteractiveSegmenterSettingsInternal
    ): Future<InteractiveSegmenter> {
        val future = CompletableFuture<InteractiveSegmenter>()

        downloadNpuModuleIfNeeded(soc).whenComplete { dispatchLibraryPath, npuException ->
            if (npuException != null) {
                Log.e("VisionProvider", "Failed to prepare NPU module.", npuException)
                future.completeExceptionally(npuException)
                return@whenComplete
            }

            val packName = "aipack_" + model.enumName

            val existingPackLocation = aiPackManager.getPackLocation(packName)

            if (existingPackLocation != null) {
                Log.d("VisionProvider", "AI Pack '$packName' is already installed. Skipping download.")
                val modelPath =
                    getAbsoluteAiAssetPath(context, packName, model.createModelFileName())

                Executors.newSingleThreadExecutor().submit {
                    try {
                        val segmenter = createSegmenterFromPath(
                            context, modelPath, dispatchLibraryPath, settings
                        )
                        future.complete(segmenter)
                    } catch (e: Exception) {
                        future.completeExceptionally(e)
                    }
                }
            } else {
                Log.d("VisionProvider", "AI Pack '$packName' not found. Starting download.")
                initiateAIPackDownload(future, packName, model, dispatchLibraryPath, settings)
            }
        }
        return future
    }

    /**
     * Initiates the download process for a given AI Pack.
     */
    private fun initiateAIPackDownload(
        future: CompletableFuture<InteractiveSegmenter>,
        packName: String,
        model: VisionModel,
        dispatchLibraryPath: String?,
        settings: InteractiveSegmenterSettingsInternal
    ) {
        val downloader = AIPackDownloader(context)
        downloader.setListener(object : AIPackDownloader.DownloadListener {
            override fun onStatusUpdate(status: DownloadStatus) {
                when (status) {
                    is DownloadStatus.Completed -> {
                        Log.d("VisionProvider", "AI Pack '$packName' downloaded successfully.")
                        notifyModelListeners(model) { it.onCompleted() }

                        val modelPath =
                            getAbsoluteAiAssetPath(context, packName, model.createModelFileName())

                        Executors.newSingleThreadExecutor().submit {
                            try {
                                val segmenter = createSegmenterFromPath(
                                    context, modelPath, dispatchLibraryPath, settings
                                )
                                future.complete(segmenter)
                            } catch (e: Exception) {
                                future.completeExceptionally(e)
                            } finally {
                                downloader.removeListener()
                            }
                        }
                    }

                    is DownloadStatus.Failed -> {
                        val error = RuntimeException("Failed to download AI Pack '$packName' with error: ${status.errorCode}")
                        Log.e("VisionProvider", error.message!!)
                        notifyModelListeners(model) { it.onFailed(error) }
                        future.completeExceptionally(error)
                        downloader.removeListener()
                    }

                    is DownloadStatus.Downloading -> {
                        Log.i(
                            "VisionProvider",
                            "Downloading '${packName}': ${status.progress}%"
                        )
                        notifyModelListeners(model) { it.onProgress(status.progress) }
                    }

                    is DownloadStatus.Idle -> {
                        // The downloader is idle, waiting for the download to start.
                    }
                }
            }

            override fun onShowConfirmationDialog(activity: Activity, status: AssetPackState) {
                // This provider class cannot show a UI dialog.
                // We fail the future and let the calling UI layer handle the user confirmation.
                val error =
                    IllegalStateException("User confirmation required to download '${packName}'. The UI must handle this.")
                Log.w("VisionProvider", error.message!!)
                notifyModelListeners(model) { it.onFailed(error) }
                future.completeExceptionally(error)
                downloader.removeListener()
            }
        })

        downloader.downloadPack(packName)
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
        const val DEFAULT_OUTPUT_CATEGORY_MASK: Boolean = false
        const val DEFAULT_OUTPUT_SEGMENTATION_MASKS: Boolean = false
    }
}
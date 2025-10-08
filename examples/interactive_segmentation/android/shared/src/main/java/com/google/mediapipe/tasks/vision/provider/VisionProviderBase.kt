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
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import android.os.Build.SOC_MODEL
import com.google.mediapipe.tasks.core.Delegate


open class VisionProviderBase(private val context: Context) {
    private var aiPackManager: AiPackManager = AiPackManagerFactory.getInstance(context.applicationContext)
    private var soc: String

    init {

        val splitInstallManager: SplitInstallManager = SplitInstallManagerFactory.create(context)
        val installedModules: Set<String> = splitInstallManager.installedModules
        if (installedModules.isEmpty()) {
            Log.d("AiPackInfo", "No feature modules are currently installed.")
        } else {
            Log.d("AiPackInfo", "Installed modules:")
            installedModules.forEach { moduleName ->
                Log.d("AiPackInfo", "- $moduleName")
            }
        }

        soc   = getHexagonVersionForSoC(SOC_MODEL)
    }

    /**
     * Downloads a Play Feature Delivery module if it's not already installed.
     *
     * @param moduleName The name of the feature module to download.
     * @return A CompletableFuture that completes when the module is installed or fails.
     */
    private fun downloadNpuModuleIfNeeded(moduleName: String): CompletableFuture<Void?> {
        val future = CompletableFuture<Void?>()
        val splitInstallManager: SplitInstallManager = SplitInstallManagerFactory.create(context)

        if (splitInstallManager.installedModules.contains(moduleName)) {
            Log.d("VisionProvider", "NPU module '$moduleName' is already installed.")
            future.complete(null)
            return future
        }

        Log.d("VisionProvider", "Requesting download for NPU module: '$moduleName'")
        val request = SplitInstallRequest.newBuilder().addModule(moduleName).build()

        val listener = SplitInstallStateUpdatedListener { state ->
            when (state.status()) {
                SplitInstallSessionStatus.INSTALLED -> {
                    Log.d("VisionProvider", "NPU module '$moduleName' installed successfully.")
                    future.complete(null)
                }
                SplitInstallSessionStatus.FAILED -> {
                    val errorMessage = "Failed to download NPU module '$moduleName' with error code: ${state.errorCode()}"
                    Log.e("VisionProvider", errorMessage)
                    future.completeExceptionally(RuntimeException(errorMessage))
                }
                SplitInstallSessionStatus.DOWNLOADING -> {
                    val progress = (state.bytesDownloaded() * 100 / state.totalBytesToDownload()).toInt()
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

    fun getHexagonVersionForSoC(socIdentifier: String): String {
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

            // Add other mappings here as needed...

            else -> return ""// Return null if the SoC is not in our list
        }
    }

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

        downloadNpuModuleIfNeeded("mediapipe_tasks_delegate_" + soc).whenComplete { dispatchLibraryPath, npuException ->
            if (npuException != null) {
                Log.e("VisionProvider", "Failed to prepare NPU module.", npuException)
                future.completeExceptionally(npuException)
                return@whenComplete
            }

            val downloader = AIPackDownloader(context)

            val packName = "aipack-" + model.enumName.lowercase().replace("_", "-")
            val modelPath = getAbsoluteAiAssetPath(packName, model.createModelFileName())

            downloader.setListener(object : AIPackDownloader.DownloadListener {
                override fun onStatusUpdate(status: DownloadStatus) {
                    when (status) {
                        is DownloadStatus.Completed -> {
                            Log.d(
                                "VisionProvider",
                                "AI Pack '${packName}' downloaded successfully."
                            )
                            // Once the download is complete, the asset pack is available to the app's
                            // AssetManager. We can now proceed with creating the MediaPipe task.
                            Executors.newSingleThreadExecutor().submit {
                                try {
                                    val baseOptionsBuilder = BaseOptions.builder()
                                        .setModelAssetPath(modelPath)

                                    if (dispatchLibraryPath) {
                                        baseOptionsBuilder.setDelegate(Delegate.NPU).setDelegateOptions(
                                            BaseOptions.DelegateOptions.npuOptions())
                                    }
                                    // runnign mode?

                                    val optionsBuilder =
                                        InteractiveSegmenter.InteractiveSegmenterOptions.builder()
                                            .setBaseOptions(baseOptionsBuilder.build())
                                            .setOutputConfidenceMasks(settings.outputConfidenceMasks())
                                            .setOutputCategoryMask(settings.outputCategoryMask())

                                    val segmenter = InteractiveSegmenter.createFromOptions(
                                        context,
                                        optionsBuilder.build()
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
                            val errorMessage =
                                "Failed to download AI Pack '${packName}' with error code: ${status.errorCode}"
                            Log.e("VisionProvider", errorMessage)
                            future.completeExceptionally(RuntimeException(errorMessage))
                            downloader.removeListener()
                        }

                        is DownloadStatus.Downloading -> {
                            // You can log progress, but the Future doesn't support progress updates.
                            Log.i(
                                "VisionProvider",
                                "Downloading '${packName}': ${status.progress}%"
                            )
                        }

                        is DownloadStatus.Idle -> {
                            // The downloader is idle, waiting for the download to start.
                        }
                    }
                }

                override fun onShowConfirmationDialog(activity: Activity, status: AssetPackState) {
                    // This provider class cannot show a UI dialog.
                    // We fail the future and let the calling UI layer handle the user confirmation.
                    val errorMessage =
                        "User confirmation required to download '${packName}'. The UI must handle this."
                    Log.w("VisionProvider", errorMessage)
                    future.completeExceptionally(IllegalStateException(errorMessage))
                    downloader.removeListener()
                }
            })


            // Start the download process.
            Log.d("VisionProvider", "Requesting download for AI Pack: '${packName}'")
            downloader.downloadPack(packName)
        }

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
}


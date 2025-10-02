package com.google.mediapipe.tasks.vision.provider;

import android.content.Context;
import android.webkit.DownloadListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.mediapipe.tasks.components.processors.ClassifierOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector;
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker;
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier;
import com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedder;
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter;
import com.google.mediapipe.tasks.vision.interactivesegmenter.InteractiveSegmenter;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;

import java.util.List;
import java.util.concurrent.Future;

class VisionProviderBase {
    // Reusable Constants
     static final float DEFAULT_CONFIDENCE = 0.5f;
     static final int DEFAULT_NUM_RESULTS = 1;
     static final int UNLIMITED_RESULTS = -1;
     static final boolean DEFAULT_OUTPUT_BLENDSHAPES = false;

    static final boolean DEFAULT_OUTPUT_FACIAL_TRANSFORMATION_MATRIXES = false;

     static final RunningMode DEFAULT_RUNNING_MODE = RunningMode.IMAGE;
    @Nullable
     static final String DEFAULT_DISPLAY_NAMES_LOCALE = null;
    @Nullable
     static final List<String> DEFAULT_CATEGORY_LIST = null;
    @Nullable
     static final ClassifierOptions DEFAULT_CLASSIFIER_OPTIONS = null;

    static final boolean DEFAULT_L2_NORMALIZE = false;
    static final boolean DEFAULT_QUANTIZE = false;
    static final boolean DEFAULT_OUTPUT_CONFIDENCE_MASKS = false;
    static final boolean DEFAULT_OUTPUT_CATEGORY_MASK = false;
    static final boolean DEFAULT_OUTPUT_SEGMENTATION_MASKS = false;

    private final Context context;

    VisionProviderBase(Context context) {
        this.context = context;
    }

    record FaceDetectorSettingsInternal(
            float minDetectionConfidence,
            float minSuppressionThreshold,
            @NonNull RunningMode runningMode
    ) {
        public FaceDetectorSettingsInternal() {
            this(DEFAULT_CONFIDENCE, DEFAULT_CONFIDENCE, DEFAULT_RUNNING_MODE);
        }
    }

    record FaceLandmarkerSettingsInternal(
            float minFaceDetectionConfidence,
            float minFacePresenceConfidence,
            float minTrackingConfidence,
            int numFaces,
            boolean outputFaceBlendshapes,
            boolean outputFacialTransformationMatrixes,
            @NonNull RunningMode runningMode
    ) {
        public FaceLandmarkerSettingsInternal() {
            this(
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_NUM_RESULTS,
                    DEFAULT_OUTPUT_BLENDSHAPES,
                    DEFAULT_OUTPUT_FACIAL_TRANSFORMATION_MATRIXES,
                    DEFAULT_RUNNING_MODE
            );
        }
    }

    record GestureRecognizerSettingsInternal(
            float minHandDetectionConfidence,
            float minHandPresenceConfidence,
            float minTrackingConfidence,
            int numHands,
            ClassifierOptions cannedGesturesClassifierOptions,
            ClassifierOptions customGesturesClassifierOptions,
            @NonNull RunningMode runningMode
    ) {
        public GestureRecognizerSettingsInternal() {
            this(
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_NUM_RESULTS,
                    DEFAULT_CLASSIFIER_OPTIONS,
                    DEFAULT_CLASSIFIER_OPTIONS,
                    DEFAULT_RUNNING_MODE
            );
        }
    }

    record HandLandmarkerSettingsInternal(
            float minHandDetectionConfidence,
            float minHandPresenceConfidence,
            float minTrackingConfidence,
            int numHands,
            @NonNull RunningMode runningMode
    ) {
        public HandLandmarkerSettingsInternal() {
            this(
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_NUM_RESULTS,
                    DEFAULT_RUNNING_MODE
            );
        }
    }

    record ImageClassifierSettingsInternal(
            String displayNamesLocale,
            int maxResults,
            float scoreThreshold,
            List<String> categoryAllowlist,
            List<String> categoryDenylist,
            @NonNull RunningMode runningMode
    ) {
        public ImageClassifierSettingsInternal() {
            this(
                    DEFAULT_DISPLAY_NAMES_LOCALE,
                    UNLIMITED_RESULTS,
                    0.0f, // Specific default, not reusing DEFAULT_CONFIDENCE
                    DEFAULT_CATEGORY_LIST,
                    DEFAULT_CATEGORY_LIST,
                    DEFAULT_RUNNING_MODE
            );
        }
    }

    record ImageEmbedderSettingsInternal(
            boolean l2Normalize,
            boolean quantize,
            @NonNull RunningMode runningMode
    ) {
        public ImageEmbedderSettingsInternal() {
            this(DEFAULT_L2_NORMALIZE, DEFAULT_QUANTIZE, DEFAULT_RUNNING_MODE);
        }
    }

    record ObjectDetectorSettingsInternal(
            String displayNamesLocale,
            int maxResults,
            float scoreThreshold,
            List<String> categoryAllowlist,
            List<String> categoryDenylist,
            @NonNull RunningMode runningMode
    ) {
        public ObjectDetectorSettingsInternal() {
            this(
                    DEFAULT_DISPLAY_NAMES_LOCALE,
                    UNLIMITED_RESULTS,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CATEGORY_LIST,
                    DEFAULT_CATEGORY_LIST,
                    DEFAULT_RUNNING_MODE
            );
        }
    }

    record PoseLandmarkerSettingsInternal(
            float minPoseDetectionConfidence,
            float minPosePresenceConfidence,
            float minTrackingConfidence,
            int numPoses,
            boolean outputSegmentationMasks,
            @NonNull RunningMode runningMode
    ) {
        public PoseLandmarkerSettingsInternal() {
            this(
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_NUM_RESULTS,
                    DEFAULT_OUTPUT_SEGMENTATION_MASKS,
                    DEFAULT_RUNNING_MODE
            );
        }
    }

    record InteractiveSegmenterSettingsInternal(
            boolean outputConfidenceMasks,
            boolean outputCategoryMask,
            @NonNull RunningMode runningMode
    ) {
        public InteractiveSegmenterSettingsInternal() {
            this(DEFAULT_OUTPUT_CONFIDENCE_MASKS, DEFAULT_OUTPUT_CATEGORY_MASK, DEFAULT_RUNNING_MODE);
        }
    }

    record ImageSegmenterSettingsInternal(
            boolean outputConfidenceMasks,
            boolean outputCategoryMask,
            String displayNamesLocale,
            @NonNull RunningMode runningMode
    ) {
        public ImageSegmenterSettingsInternal() {
            this(
                    DEFAULT_OUTPUT_CONFIDENCE_MASKS,
                    DEFAULT_OUTPUT_CATEGORY_MASK,
                    DEFAULT_DISPLAY_NAMES_LOCALE,
                    DEFAULT_RUNNING_MODE
            );
        }
    }

    /**
     * Adds a listener for model download events.
     *
     * @param listener The listener to add.
     */
    public static void addModelDownloadListener(@NonNull VisionModel model, @NonNull DownloadListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes a previously added model download listener.
     *
     * @param listener The listener to remove.
     */
    public void removeModelDownloadListener(@NonNull VisionModel model, @NonNull DownloadListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds a listener for NPU delegate initialization events.
     *
     * @param listener The listener to add.
     */
    public void addNpuDelegateDownloadListener(@NonNull DownloadListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes a previously added NPU delegate initialization listener.
     *
     * @param listener The listener to remove.
     */
    public void removeNpuDelegateDownloadsListener(@NonNull DownloadListener listener) {
        throw new UnsupportedOperationException();
    }


    public Future<FaceDetector> createFaceDetectorImpl(VisionModel model, FaceDetectorSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<FaceLandmarker> createFaceLandmarkerImpl(VisionModel model, FaceLandmarkerSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<GestureRecognizer> createGestureRecognizerImpl(VisionModel model, GestureRecognizerSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<HandLandmarker> createHandLandmarkerImpl(VisionModel model, HandLandmarkerSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<ImageClassifier> createImageClassifierImpl(VisionModel model, ImageClassifierSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<ImageEmbedder> createImageEmbedderImpl(VisionModel model, ImageEmbedderSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<ImageSegmenter> createImageSegmenterImpl(VisionModel model, ImageSegmenterSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<InteractiveSegmenter> createInteractiveSegmenterImpl(VisionModel model, InteractiveSegmenterSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<ObjectDetector> createObjectDetectorImpl(VisionModel model, ObjectDetectorSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<PoseLandmarker> createPoseLandmarkerImpl(VisionModel model, PoseLandmarkerSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }
}
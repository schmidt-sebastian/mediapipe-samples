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
import java.util.Objects;
import java.util.concurrent.Future;

public class VisionProviderBase {
    // Reusable Constants
    static final float DEFAULT_CONFIDENCE = 0.5f;
    static final int DEFAULT_NUM_RESULTS = 1;
    static final int UNLIMITED_RESULTS = -1;
    static final boolean DEFAULT_OUTPUT_BLENDSHAPES = false;
    static final boolean DEFAULT_OUTPUT_FACIAL_TRANSFORMATION_MATRIXES = false;
    static final RunningMode DEFAULT_RUNNING_MODE = RunningMode.IMAGE;
    @Nullable static final String DEFAULT_DISPLAY_NAMES_LOCALE = null;
    @Nullable static final List<String> DEFAULT_CATEGORY_LIST = null;
    @Nullable static final ClassifierOptions DEFAULT_CLASSIFIER_OPTIONS = null;
    static final boolean DEFAULT_L2_NORMALIZE = false;
    static final boolean DEFAULT_QUANTIZE = false;
    static final boolean DEFAULT_OUTPUT_CONFIDENCE_MASKS = false;
    static final boolean DEFAULT_OUTPUT_CATEGORY_MASK = false;
    static final boolean DEFAULT_OUTPUT_SEGMENTATION_MASKS = false;

    private final Context context;

    public VisionProviderBase(Context context) {
        this.context = context;
    }

    static final class FaceDetectorSettingsInternal {
        private final float minDetectionConfidence;
        private final float minSuppressionThreshold;
        @NonNull private final RunningMode runningMode;

        public FaceDetectorSettingsInternal(
                float minDetectionConfidence, float minSuppressionThreshold, @NonNull RunningMode runningMode) {
            this.minDetectionConfidence = minDetectionConfidence;
            this.minSuppressionThreshold = minSuppressionThreshold;
            this.runningMode = runningMode;
        }

        public FaceDetectorSettingsInternal() {
            this(DEFAULT_CONFIDENCE, DEFAULT_CONFIDENCE, DEFAULT_RUNNING_MODE);
        }

        public float minDetectionConfidence() {
            return minDetectionConfidence;
        }

        public float minSuppressionThreshold() {
            return minSuppressionThreshold;
        }

        @NonNull
        public RunningMode runningMode() {
            return runningMode;
        }
    }

    static final class FaceLandmarkerSettingsInternal {
        private final float minFaceDetectionConfidence;
        private final float minFacePresenceConfidence;
        private final float minTrackingConfidence;
        private final int numFaces;
        private final boolean outputFaceBlendshapes;
        private final boolean outputFacialTransformationMatrixes;
        @NonNull private final RunningMode runningMode;

        public FaceLandmarkerSettingsInternal(
                float minFaceDetectionConfidence,
                float minFacePresenceConfidence,
                float minTrackingConfidence,
                int numFaces,
                boolean outputFaceBlendshapes,
                boolean outputFacialTransformationMatrixes,
                @NonNull RunningMode runningMode) {
            this.minFaceDetectionConfidence = minFaceDetectionConfidence;
            this.minFacePresenceConfidence = minFacePresenceConfidence;
            this.minTrackingConfidence = minTrackingConfidence;
            this.numFaces = numFaces;
            this.outputFaceBlendshapes = outputFaceBlendshapes;
            this.outputFacialTransformationMatrixes = outputFacialTransformationMatrixes;
            this.runningMode = runningMode;
        }

        public FaceLandmarkerSettingsInternal() {
            this(
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_NUM_RESULTS,
                    DEFAULT_OUTPUT_BLENDSHAPES,
                    DEFAULT_OUTPUT_FACIAL_TRANSFORMATION_MATRIXES,
                    DEFAULT_RUNNING_MODE);
        }

        public float minFaceDetectionConfidence() {
            return minFaceDetectionConfidence;
        }

        public float minFacePresenceConfidence() {
            return minFacePresenceConfidence;
        }

        public float minTrackingConfidence() {
            return minTrackingConfidence;
        }

        public int numFaces() {
            return numFaces;
        }

        public boolean outputFaceBlendshapes() {
            return outputFaceBlendshapes;
        }

        public boolean outputFacialTransformationMatrixes() {
            return outputFacialTransformationMatrixes;
        }

        @NonNull
        public RunningMode runningMode() {
            return runningMode;
        }
    }

    static final class GestureRecognizerSettingsInternal {
        private final float minHandDetectionConfidence;
        private final float minHandPresenceConfidence;
        private final float minTrackingConfidence;
        private final int numHands;
        private final ClassifierOptions cannedGesturesClassifierOptions;
        private final ClassifierOptions customGesturesClassifierOptions;
        @NonNull private final RunningMode runningMode;

        public GestureRecognizerSettingsInternal(
                float minHandDetectionConfidence,
                float minHandPresenceConfidence,
                float minTrackingConfidence,
                int numHands,
                ClassifierOptions cannedGesturesClassifierOptions,
                ClassifierOptions customGesturesClassifierOptions,
                @NonNull RunningMode runningMode) {
            this.minHandDetectionConfidence = minHandDetectionConfidence;
            this.minHandPresenceConfidence = minHandPresenceConfidence;
            this.minTrackingConfidence = minTrackingConfidence;
            this.numHands = numHands;
            this.cannedGesturesClassifierOptions = cannedGesturesClassifierOptions;
            this.customGesturesClassifierOptions = customGesturesClassifierOptions;
            this.runningMode = runningMode;
        }

        public GestureRecognizerSettingsInternal() {
            this(
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_NUM_RESULTS,
                    DEFAULT_CLASSIFIER_OPTIONS,
                    DEFAULT_CLASSIFIER_OPTIONS,
                    DEFAULT_RUNNING_MODE);
        }

        public float minHandDetectionConfidence() {
            return minHandDetectionConfidence;
        }

        public float minHandPresenceConfidence() {
            return minHandPresenceConfidence;
        }

        public float minTrackingConfidence() {
            return minTrackingConfidence;
        }

        public int numHands() {
            return numHands;
        }

        public ClassifierOptions cannedGesturesClassifierOptions() {
            return cannedGesturesClassifierOptions;
        }

        public ClassifierOptions customGesturesClassifierOptions() {
            return customGesturesClassifierOptions;
        }

        @NonNull
        public RunningMode runningMode() {
            return runningMode;
        }
    }

    static final class HandLandmarkerSettingsInternal {
        private final float minHandDetectionConfidence;
        private final float minHandPresenceConfidence;
        private final float minTrackingConfidence;
        private final int numHands;
        @NonNull private final RunningMode runningMode;

        public HandLandmarkerSettingsInternal(
                float minHandDetectionConfidence,
                float minHandPresenceConfidence,
                float minTrackingConfidence,
                int numHands,
                @NonNull RunningMode runningMode) {
            this.minHandDetectionConfidence = minHandDetectionConfidence;
            this.minHandPresenceConfidence = minHandPresenceConfidence;
            this.minTrackingConfidence = minTrackingConfidence;
            this.numHands = numHands;
            this.runningMode = runningMode;
        }

        public HandLandmarkerSettingsInternal() {
            this(
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_NUM_RESULTS,
                    DEFAULT_RUNNING_MODE);
        }

        public float minHandDetectionConfidence() {
            return minHandDetectionConfidence;
        }

        public float minHandPresenceConfidence() {
            return minHandPresenceConfidence;
        }

        public float minTrackingConfidence() {
            return minTrackingConfidence;
        }

        public int numHands() {
            return numHands;
        }

        @NonNull
        public RunningMode runningMode() {
            return runningMode;
        }
    }

    static final class ImageClassifierSettingsInternal {
        private final String displayNamesLocale;
        private final int maxResults;
        private final float scoreThreshold;
        private final List<String> categoryAllowlist;
        private final List<String> categoryDenylist;
        @NonNull private final RunningMode runningMode;

        public ImageClassifierSettingsInternal(
                String displayNamesLocale,
                int maxResults,
                float scoreThreshold,
                List<String> categoryAllowlist,
                List<String> categoryDenylist,
                @NonNull RunningMode runningMode) {
            this.displayNamesLocale = displayNamesLocale;
            this.maxResults = maxResults;
            this.scoreThreshold = scoreThreshold;
            this.categoryAllowlist = categoryAllowlist;
            this.categoryDenylist = categoryDenylist;
            this.runningMode = runningMode;
        }

        public ImageClassifierSettingsInternal() {
            this(
                    DEFAULT_DISPLAY_NAMES_LOCALE,
                    UNLIMITED_RESULTS,
                    0.0f, // Specific default, not reusing DEFAULT_CONFIDENCE
                    DEFAULT_CATEGORY_LIST,
                    DEFAULT_CATEGORY_LIST,
                    DEFAULT_RUNNING_MODE);
        }

        public String displayNamesLocale() {
            return displayNamesLocale;
        }

        public int maxResults() {
            return maxResults;
        }

        public float scoreThreshold() {
            return scoreThreshold;
        }

        public List<String> categoryAllowlist() {
            return categoryAllowlist;
        }

        public List<String> categoryDenylist() {
            return categoryDenylist;
        }

        @NonNull
        public RunningMode runningMode() {
            return runningMode;
        }
    }

    static final class ImageEmbedderSettingsInternal {
        private final boolean l2Normalize;
        private final boolean quantize;
        @NonNull private final RunningMode runningMode;

        public ImageEmbedderSettingsInternal(
                boolean l2Normalize, boolean quantize, @NonNull RunningMode runningMode) {
            this.l2Normalize = l2Normalize;
            this.quantize = quantize;
            this.runningMode = runningMode;
        }

        public ImageEmbedderSettingsInternal() {
            this(DEFAULT_L2_NORMALIZE, DEFAULT_QUANTIZE, DEFAULT_RUNNING_MODE);
        }

        public boolean l2Normalize() {
            return l2Normalize;
        }

        public boolean quantize() {
            return quantize;
        }

        @NonNull
        public RunningMode runningMode() {
            return runningMode;
        }
    }

    static final class ObjectDetectorSettingsInternal {
        private final String displayNamesLocale;
        private final int maxResults;
        private final float scoreThreshold;
        private final List<String> categoryAllowlist;
        private final List<String> categoryDenylist;
        @NonNull private final RunningMode runningMode;

        public ObjectDetectorSettingsInternal(
                String displayNamesLocale,
                int maxResults,
                float scoreThreshold,
                List<String> categoryAllowlist,
                List<String> categoryDenylist,
                @NonNull RunningMode runningMode) {
            this.displayNamesLocale = displayNamesLocale;
            this.maxResults = maxResults;
            this.scoreThreshold = scoreThreshold;
            this.categoryAllowlist = categoryAllowlist;
            this.categoryDenylist = categoryDenylist;
            this.runningMode = runningMode;
        }

        public ObjectDetectorSettingsInternal() {
            this(
                    DEFAULT_DISPLAY_NAMES_LOCALE,
                    UNLIMITED_RESULTS,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CATEGORY_LIST,
                    DEFAULT_CATEGORY_LIST,
                    DEFAULT_RUNNING_MODE);
        }

        public String displayNamesLocale() {
            return displayNamesLocale;
        }

        public int maxResults() {
            return maxResults;
        }

        public float scoreThreshold() {
            return scoreThreshold;
        }

        public List<String> categoryAllowlist() {
            return categoryAllowlist;
        }

        public List<String> categoryDenylist() {
            return categoryDenylist;
        }

        @NonNull
        public RunningMode runningMode() {
            return runningMode;
        }
    }

    static final class PoseLandmarkerSettingsInternal {
        private final float minPoseDetectionConfidence;
        private final float minPosePresenceConfidence;
        private final float minTrackingConfidence;
        private final int numPoses;
        private final boolean outputSegmentationMasks;
        @NonNull private final RunningMode runningMode;

        public PoseLandmarkerSettingsInternal(
                float minPoseDetectionConfidence,
                float minPosePresenceConfidence,
                float minTrackingConfidence,
                int numPoses,
                boolean outputSegmentationMasks,
                @NonNull RunningMode runningMode) {
            this.minPoseDetectionConfidence = minPoseDetectionConfidence;
            this.minPosePresenceConfidence = minPosePresenceConfidence;
            this.minTrackingConfidence = minTrackingConfidence;
            this.numPoses = numPoses;
            this.outputSegmentationMasks = outputSegmentationMasks;
            this.runningMode = runningMode;
        }

        public PoseLandmarkerSettingsInternal() {
            this(
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_CONFIDENCE,
                    DEFAULT_NUM_RESULTS,
                    DEFAULT_OUTPUT_SEGMENTATION_MASKS,
                    DEFAULT_RUNNING_MODE);
        }

        public float minPoseDetectionConfidence() {
            return minPoseDetectionConfidence;
        }

        public float minPosePresenceConfidence() {
            return minPosePresenceConfidence;
        }

        public float minTrackingConfidence() {
            return minTrackingConfidence;
        }

        public int numPoses() {
            return numPoses;
        }

        public boolean outputSegmentationMasks() {
            return outputSegmentationMasks;
        }

        @NonNull
        public RunningMode runningMode() {
            return runningMode;
        }
    }

    static final class InteractiveSegmenterSettingsInternal {
        private final boolean outputConfidenceMasks;
        private final boolean outputCategoryMask;
        @NonNull private final RunningMode runningMode;

        public InteractiveSegmenterSettingsInternal(
                boolean outputConfidenceMasks, boolean outputCategoryMask, @NonNull RunningMode runningMode) {
            this.outputConfidenceMasks = outputConfidenceMasks;
            this.outputCategoryMask = outputCategoryMask;
            this.runningMode = runningMode;
        }

        public InteractiveSegmenterSettingsInternal() {
            this(DEFAULT_OUTPUT_CONFIDENCE_MASKS, DEFAULT_OUTPUT_CATEGORY_MASK, DEFAULT_RUNNING_MODE);
        }

        public boolean outputConfidenceMasks() {
            return outputConfidenceMasks;
        }

        public boolean outputCategoryMask() {
            return outputCategoryMask;
        }

        @NonNull
        public RunningMode runningMode() {
            return runningMode;
        }
    }

    static final class ImageSegmenterSettingsInternal {
        private final boolean outputConfidenceMasks;
        private final boolean outputCategoryMask;
        private final String displayNamesLocale;
        @NonNull private final RunningMode runningMode;

        public ImageSegmenterSettingsInternal(
                boolean outputConfidenceMasks,
                boolean outputCategoryMask,
                String displayNamesLocale,
                @NonNull RunningMode runningMode) {
            this.outputConfidenceMasks = outputConfidenceMasks;
            this.outputCategoryMask = outputCategoryMask;
            this.displayNamesLocale = displayNamesLocale;
            this.runningMode = runningMode;
        }

        public ImageSegmenterSettingsInternal() {
            this(
                    DEFAULT_OUTPUT_CONFIDENCE_MASKS,
                    DEFAULT_OUTPUT_CATEGORY_MASK,
                    DEFAULT_DISPLAY_NAMES_LOCALE,
                    DEFAULT_RUNNING_MODE);
        }

        public boolean outputConfidenceMasks() {
            return outputConfidenceMasks;
        }

        public boolean outputCategoryMask() {
            return outputCategoryMask;
        }

        public String displayNamesLocale() {
            return displayNamesLocale;
        }

        @NonNull
        public RunningMode runningMode() {
            return runningMode;
        }
    }

    /**
     * Adds a listener for model download events.
     *
     * @param listener The listener to add.
     */
    public static void addModelDownloadListener(
            @NonNull VisionModel model, @NonNull DownloadListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes a previously added model download listener.
     *
     * @param listener The listener to remove.
     */
    public void removeModelDownloadListener(
            @NonNull VisionModel model, @NonNull DownloadListener listener) {
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

    public Future<FaceDetector> createFaceDetectorImpl(
            VisionModel model, FaceDetectorSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<FaceLandmarker> createFaceLandmarkerImpl(
            VisionModel model, FaceLandmarkerSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<GestureRecognizer> createGestureRecognizerImpl(
            VisionModel model, GestureRecognizerSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<HandLandmarker> createHandLandmarkerImpl(
            VisionModel model, HandLandmarkerSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<ImageClassifier> createImageClassifierImpl(
            VisionModel model, ImageClassifierSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<ImageEmbedder> createImageEmbedderImpl(
            VisionModel model, ImageEmbedderSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<ImageSegmenter> createImageSegmenterImpl(
            VisionModel model, ImageSegmenterSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<InteractiveSegmenter> createInteractiveSegmenterImpl(
            VisionModel model, InteractiveSegmenterSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<ObjectDetector> createObjectDetectorImpl(
            VisionModel model, ObjectDetectorSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }

    public Future<PoseLandmarker> createPoseLandmarkerImpl(
            VisionModel model, PoseLandmarkerSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }
}
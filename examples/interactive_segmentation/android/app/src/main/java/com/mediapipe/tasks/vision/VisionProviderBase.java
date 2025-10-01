package com.mediapipe.tasks.vision;

import android.content.Context;
import android.webkit.DownloadListener;

import androidx.annotation.NonNull;

import com.google.mediapipe.tasks.components.processors.ClassifierOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector;

import java.util.List;
import java.util.concurrent.Future;

class VisionProviderBase {
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
            this(0.5f, 0.5f, RunningMode.IMAGE);
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
            this(0.5f, 0.5f, 0.5f, 1, false, false, RunningMode.IMAGE);
        }
    }

    record FaceStylizerSettingsInternal(
            @NonNull RunningMode runningMode
    ) {
        public FaceStylizerSettingsInternal() {
            this(RunningMode.IMAGE);
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
            this(0.5f, 0.5f, 0.5f, 1, new ClassifierOptions(), new ClassifierOptions(), RunningMode.IMAGE);
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
            this(0.5f, 0.5f, 0.5f, 1, RunningMode.IMAGE);
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
            this("en", -1, 0.0f, new ArrayList<>(), new ArrayList<>(), RunningMode.IMAGE);
        }
    }

    record ImageEmbedderSettingsInternal(
            boolean l2Normalize,
            boolean quantize,
            @NonNull RunningMode runningMode
    ) {
        public ImageEmbedderSettingsInternal() {
            this(false, false, RunningMode.IMAGE);
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
            this("en", -1, 0.0f, new ArrayList<>(), new ArrayList<>(), RunningMode.IMAGE);
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
            this(0.5f, 0.5f, 0.5f, 1, false, RunningMode.IMAGE);
        }
    }

    record InteractiveSegmenterSettingsInternal(
            boolean outputConfidenceMasks,
            boolean outputCategoryMask,
            @NonNull RunningMode runningMode
    ) {
        public InteractiveSegmenterSettingsInternal() {
            this(true, true, RunningMode.IMAGE);
        }
    }

    record ImageSegmenterSettingsInternal(
            boolean outputConfidenceMasks,
            boolean outputCategoryMask,
            String displayNamesLocale,
            @NonNull RunningMode runningMode
    ) {
        public ImageSegmenterSettingsInternal() {
            this(true, true, "en", RunningMode.IMAGE);
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
    public void removeModelDownloadListener(@NonNull VisionModel model, @@NonNull DownloadListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds a listener for NPU delegate initialization events.
     *
     * @param listener The listener to add.
     */
    public void addNpuDelegateInitializationListener(@NonNull DownloadListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes a previously added NPU delegate initialization listener.
     *
     * @param listener The listener to remove.
     */
    public void removeNpuDelegateInitializationListener(@NonNull DownloadListener listener) {
        throw new UnsupportedOperationException();
    }


    public Future<FaceDetector> createFaceDetectorImpl(VisionModel model, FaceDetectorSettingsInternal settings) {
        throw new UnsupportedOperationException();
    }
}

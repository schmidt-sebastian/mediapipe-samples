package com.mediapipe.tasks.vision;

import android.content.Context;
import androidx.annotation.NonNull;

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
import com.mediapipe.tasks.core.Quantization;

import java.util.List;
import java.util.concurrent.Future;

public class VisionProvider extends VisionProviderBase {

    VisionProvider(Context context) {
        super(context);
    }

    public static VisionProvider create(@NonNull Context context) {
        return new VisionProvider(context);
    }

    //region Models
    public enum FaceDetectorModel implements VisionModel {
        BLAZE_FACE_SHORT_RANGE_F32_V1("blaze_face_short_range", "1", Quantization.FLOAT32);

        private final String modelName;
        private final String version;
        private final Quantization quantization;

        FaceDetectorModel(String modelName, String version, Quantization quantization) {
            this.modelName = modelName;
            this.version = version;
            this.quantization = quantization;
        }

        @Override
        public String getModelName() {
            return this.modelName;
        }

        @Override
        public String getVersion() {
            return this.version;
        }

        @Override
        public Quantization getQuantization() {
            return this.quantization;
        }
    }

    public enum FaceLandmarkerModel implements VisionModel {
        FACE_LANDMARKER_F32_V1("face_landmarker", "1", Quantization.FLOAT32),
        FACE_LANDMARKER_WITH_BLENDSHAPES_F32_V2("face_landmarker_v2_with_blendshapes", "2", Quantization.FLOAT32);

        private final String modelName;
        private final String version;
        private final Quantization quantization;

        FaceLandmarkerModel(String modelName, String version, Quantization quantization) {
            this.modelName = modelName;
            this.version = version;
            this.quantization = quantization;
        }

        @Override
        public String getModelName() {
            return this.modelName;
        }

        @Override
        public String getVersion() {
            return this.version;
        }

        @Override
        public Quantization getQuantization() {
            return this.quantization;
        }
    }

    public enum FaceStylizerModel implements VisionModel {
        COLOR_SKETCH_F32_V1("face_stylizer_color_sketch", "1", Quantization.FLOAT32),
        COLOR_INK_F32_V1("face_stylizer_color_ink", "1", Quantization.FLOAT32),
        OIL_PAINTING_F32_V1("face_stylizer_oil_painting", "1", Quantization.FLOAT32);

        private final String modelName;
        private final String version;
        private final Quantization quantization;

        FaceStylizerModel(String modelName, String version, Quantization quantization) {
            this.modelName = modelName;
            this.version = version;
            this.quantization = quantization;
        }

        @Override
        public String getModelName() {
            return this.modelName;
        }

        @Override
        public String getVersion() {
            return this.version;
        }

        @Override
        public Quantization getQuantization() {
            return this.quantization;
        }
    }

    public enum GestureRecognizerModel implements VisionModel {
        GESTURE_RECOGNIZER_F32_V1("gesture_recognizer", "1", Quantization.FLOAT32);

        private final String modelName;
        private final String version;
        private final Quantization quantization;

        GestureRecognizerModel(String modelName, String version, Quantization quantization) {
            this.modelName = modelName;
            this.version = version;
            this.quantization = quantization;
        }

        @Override
        public String getModelName() {
            return this.modelName;
        }

        @Override
        public String getVersion() {
            return this.version;
        }

        @Override
        public Quantization getQuantization() {
            return this.quantization;
        }
    }

    public enum HandLandmarkerModel implements VisionModel {
        HAND_LANDMARKER_F32_V1("hand_landmarker", "1", Quantization.FLOAT32);

        private final String modelName;
        private final String version;
        private final Quantization quantization;

        HandLandmarkerModel(String modelName, String version, Quantization quantization) {
            this.modelName = modelName;
            this.version = version;
            this.quantization = quantization;
        }

        @Override
        public String getModelName() {
            return this.modelName;
        }

        @Override
        public String getVersion() {
            return this.version;
        }

        @Override
        public Quantization getQuantization() {
            return this.quantization;
        }
    }

    public enum ImageClassifierModel implements VisionModel {
        EFFICIENTNET_LITE0_F32_V1("efficientnet_lite0", "1", Quantization.FLOAT32),
        EFFICIENTNET_LITE2_F32_V1("efficientnet_lite2", "1", Quantization.FLOAT32);

        private final String modelName;
        private final String version;
        private final Quantization quantization;

        ImageClassifierModel(String modelName, String version, Quantization quantization) {
            this.modelName = modelName;
            this.version = version;
            this.quantization = quantization;
        }

        @Override
        public String getModelName() {
            return this.modelName;
        }

        @Override
        public String getVersion() {
            return this.version;
        }

        @Override
        public Quantization getQuantization() {
            return this.quantization;
        }
    }

    public enum ImageEmbedderModel implements VisionModel {
        MOBILENET_V3_SMALL_F32_V1("mobilenet_v3_small", "1", Quantization.FLOAT32),
        MOBILENET_V3_LARGE_F32_V1("mobilenet_v3_large", "1", Quantization.FLOAT32);

        private final String modelName;
        private final String version;
        private final Quantization quantization;

        ImageEmbedderModel(String modelName, String version, Quantization quantization) {
            this.modelName = modelName;
            this.version = version;
            this.quantization = quantization;
        }

        @Override
        public String getModelName() {
            return this.modelName;
        }

        @Override
        public String getVersion() {
            return this.version;
        }

        @Override
        public Quantization getQuantization() {
            return this.quantization;
        }
    }

    public enum ObjectDetectorModel implements VisionModel {
        EFFICIENTDET_LITE0_F32_V1("efficientdet_lite0", "1", Quantization.FLOAT32),
        EFFICIENTDET_LITE2_F32_V1("efficientdet_lite2", "1", Quantization.FLOAT32);

        private final String modelName;
        private final String version;
        private final Quantization quantization;

        ObjectDetectorModel(String modelName, String version, Quantization quantization) {
            this.modelName = modelName;
            this.version = version;
            this.quantization = quantization;
        }

        @Override
        public String getModelName() {
            return this.modelName;
        }

        @Override
        public String getVersion() {
            return this.version;
        }

        @Override
        public Quantization getQuantization() {
            return this.quantization;
        }
    }

    public enum PoseLandmarkerModel implements VisionModel {
        POSE_LANDMARKER_LITE_F32_V1("pose_landmarker_lite", "1", Quantization.FLOAT32),
        POSE_LANDMARKER_FULL_F32_V1("pose_landmarker_full", "1", Quantization.FLOAT32),
        POSE_LANDMARKER_HEAVY_F32_V1("pose_landmarker_heavy", "1", Quantization.FLOAT32);

        private final String modelName;
        private final String version;
        private final Quantization quantization;

        PoseLandmarkerModel(String modelName, String version, Quantization quantization) {
            this.modelName = modelName;
            this.version = version;
            this.quantization = quantization;
        }

        @Override
        public String getModelName() {
            return this.modelName;
        }

        @Override
        public String getVersion() {
            return this.version;
        }

        @Override
        public Quantization getQuantization() {
            return this.quantization;
        }
    }

    public enum InteractiveSegmenterModel implements VisionModel {
        MAGIC_TOUCH_F32_V1("magic_touch", "1", Quantization.FLOAT32);

        private final String modelName;
        private final String version;
        private final Quantization quantization;

        InteractiveSegmenterModel(String modelName, String version, Quantization quantization) {
            this.modelName = modelName;
            this.version = version;
            this.quantization = quantization;
        }

        @Override
        public String getModelName() {
            return this.modelName;
        }

        @Override
        public String getVersion() {
            return this.version;
        }

        @Override
        public Quantization getQuantization() {
            return this.quantization;
        }
    }

    public enum ImageSegmenterModel implements VisionModel {
        MAGIC_TOUCH_F32_V1("magic_touch", "1", Quantization.FLOAT32),
        SELFIE_SEGMENTER_SQUARE_F16_V1("selfie_segmenter_square", "1", Quantization.FLOAT32),
        SELFIE_SEGMENTER_LANDSCAPE_F16_V1("selfie_segmenter_landscape", "1", Quantization.FLOAT32),
        HAIR_SEGMENTER_F32_V1("hair_segmenter", "1", Quantization.FLOAT32),
        SELFIE_MULTICLASS_F32_V1("selfie_multiclass", "1", Quantization.FLOAT32),
        DEEP_LAP_F32_V3("deep_lap", "3", Quantization.FLOAT32);

        private final String modelName;
        private final String version;
        private final Quantization quantization;

        ImageSegmenterModel(String modelName, String version, Quantization quantization) {
            this.modelName = modelName;
            this.version = version;
            this.quantization = quantization;
        }

        @Override
        public String getModelName() {
            return this.modelName;
        }

        @Override
        public String getVersion() {
            return this.version;
        }

        @Override
        public Quantization getQuantization() {
            return this.quantization;
        }
    }
    //endregion

    //region Settings
    public record FaceDetectorSettings(
            float minDetectionConfidence,
            float minSuppressionThreshold,
            @NonNull RunningMode runningMode
    ) {}

    public record FaceLandmarkerSettings(
            float minFaceDetectionConfidence,
            float minFacePresenceConfidence,
            float minTrackingConfidence,
            int numFaces,
            boolean outputFaceBlendshapes,
            boolean outputFacialTransformationMatrixes,
            @NonNull RunningMode runningMode
    ) {}


    public record GestureRecognizerSettings(
            float minHandDetectionConfidence,
            float minHandPresenceConfidence,
            float minTrackingConfidence,
            int numHands,
            ClassifierOptions cannedGesturesClassifierOptions,
            ClassifierOptions customGesturesClassifierOptions,
            @NonNull RunningMode runningMode
    ) {}

    public record HandLandmarkerSettings(
            float minHandDetectionConfidence,
            float minHandPresenceConfidence,
            float minTrackingConfidence,
            int numHands,
            @NonNull RunningMode runningMode
    ) {}

    public record ImageClassifierSettings(
            String displayNamesLocale,
            int maxResults,
            float scoreThreshold,
            List<String> categoryAllowlist,
            List<String> categoryDenylist,
            @NonNull RunningMode runningMode
    ) {}

    public record ImageEmbedderSettings(
            boolean l2Normalize,
            boolean quantize,
            @NonNull RunningMode runningMode
    ) {}

    public record ObjectDetectorSettings(
            String displayNamesLocale,
            int maxResults,
            float scoreThreshold,
            List<String> categoryAllowlist,
            List<String> categoryDenylist,
            @NonNull RunningMode runningMode
    ) {}

    public record PoseLandmarkerSettings(
            float minPoseDetectionConfidence,
            float minPosePresenceConfidence,
            float minTrackingConfidence,
            int numPoses,
            boolean outputSegmentationMasks,
            @NonNull RunningMode runningMode
    ) {}

    public record InteractiveSegmenterSettings(
            boolean outputConfidenceMasks,
            boolean outputCategoryMask,
            @NonNull RunningMode runningMode
    ) {}

    public record ImageSegmenterSettings(
            boolean outputConfidenceMasks,
            boolean outputCategoryMask,
            String displayNamesLocale,
            @NonNull RunningMode runningMode
    ) {}
    //endregion

    //region Creators
    public Future<FaceDetector> createFaceDetector() {
        return createFaceDetectorImpl(FaceDetectorModel.BLAZE_FACE_SHORT_RANGE_F32_V1, new FaceDetectorSettingsInternal());
    }
    public Future<FaceDetector> createFaceDetector(@NonNull FaceDetectorModel model) {
        return createFaceDetectorImpl(model, new FaceDetectorSettingsInternal());
    }
    public Future<FaceDetector> createFaceDetector(@NonNull FaceDetectorModel model, @NonNull FaceDetectorSettings settings) {
        return createFaceDetectorImpl(model, new FaceDetectorSettingsInternal(settings.minDetectionConfidence(), settings.minSuppressionThreshold(), settings.runningMode()));
    }

    public Future<FaceLandmarker> createFaceLandmarker() {
        return createFaceLandmarkerImpl(FaceLandmarkerModel.FACE_LANDMARKER_F32_V1, new FaceLandmarkerSettingsInternal());
    }
    public Future<FaceLandmarker> createFaceLandmarker(@NonNull FaceLandmarkerModel model) {
        return createFaceLandmarkerImpl(model, new FaceLandmarkerSettingsInternal());
    }
    public Future<FaceLandmarker> createFaceLandmarker(@NonNull FaceLandmarkerModel model, @NonNull FaceLandmarkerSettings settings) {
        return createFaceLandmarkerImpl(model, new FaceLandmarkerSettingsInternal(settings.minFaceDetectionConfidence(), settings.minFacePresenceConfidence(), settings.minTrackingConfidence(), settings.numFaces(), settings.outputFaceBlendshapes(), settings.outputFacialTransformationMatrixes(), settings.runningMode()));
    }

    public Future<GestureRecognizer> createGestureRecognizer() {
        return createGestureRecognizerImpl(GestureRecognizerModel.GESTURE_RECOGNIZER_F32_V1, new GestureRecognizerSettingsInternal());
    }
    public Future<GestureRecognizer> createGestureRecognizer(@NonNull GestureRecognizerModel model) {
        return createGestureRecognizerImpl(model, new GestureRecognizerSettingsInternal());
    }
    public Future<GestureRecognizer> createGestureRecognizer(@NonNull GestureRecognizerModel model, @NonNull GestureRecognizerSettings settings) {
        return createGestureRecognizerImpl(model, new GestureRecognizerSettingsInternal(settings.minHandDetectionConfidence(), settings.minHandPresenceConfidence(), settings.minTrackingConfidence(), settings.numHands(), settings.cannedGesturesClassifierOptions(), settings.customGesturesClassifierOptions(), settings.runningMode()));
    }

    public Future<HandLandmarker> createHandLandmarker() {
        return createHandLandmarkerImpl(HandLandmarkerModel.HAND_LANDMARKER_F32_V1, new HandLandmarkerSettingsInternal());
    }
    public Future<HandLandmarker> createHandLandmarker(@NonNull HandLandmarkerModel model) {
        return createHandLandmarkerImpl(model, new HandLandmarkerSettingsInternal());
    }
    public Future<HandLandmarker> createHandLandmarker(@NonNull HandLandmarkerModel model, @NonNull HandLandmarkerSettings settings) {
        return createHandLandmarkerImpl(model, new HandLandmarkerSettingsInternal(settings.minHandDetectionConfidence(), settings.minHandPresenceConfidence(), settings.minTrackingConfidence(), settings.numHands(), settings.runningMode()));
    }

    public Future<ImageClassifier> createImageClassifier() {
        return createImageClassifierImpl(ImageClassifierModel.EFFICIENTNET_LITE0_F32_V1, new ImageClassifierSettingsInternal());
    }
    public Future<ImageClassifier> createImageClassifier(@NonNull ImageClassifierModel model) {
        return createImageClassifierImpl(model, new ImageClassifierSettingsInternal());
    }
    public Future<ImageClassifier> createImageClassifier(@NonNull ImageClassifierModel model, @NonNull ImageClassifierSettings settings) {
        return createImageClassifierImpl(model, new ImageClassifierSettingsInternal(settings.displayNamesLocale(), settings.maxResults(), settings.scoreThreshold(), settings.categoryAllowlist(), settings.categoryDenylist(), settings.runningMode()));
    }

    public Future<ImageEmbedder> createImageEmbedder() {
        return createImageEmbedderImpl(ImageEmbedderModel.MOBILENET_V3_SMALL_F32_V1, new ImageEmbedderSettingsInternal());
    }
    public Future<ImageEmbedder> createImageEmbedder(@NonNull ImageEmbedderModel model) {
        return createImageEmbedderImpl(model, new ImageEmbedderSettingsInternal());
    }
    public Future<ImageEmbedder> createImageEmbedder(@NonNull ImageEmbedderModel model, @NonNull ImageEmbedderSettings settings) {
        return createImageEmbedderImpl(model, new ImageEmbedderSettingsInternal(settings.l2Normalize(), settings.quantize(), settings.runningMode()));
    }

    public Future<ImageSegmenter> createImageSegmenter() {
        return createImageSegmenterImpl(ImageSegmenterModel.DEEP_LAP_F32_V3, new ImageSegmenterSettingsInternal());
    }
    public Future<ImageSegmenter> createImageSegmenter(@NonNull ImageSegmenterModel model) {
        return createImageSegmenterImpl(model, new ImageSegmenterSettingsInternal());
    }
    public Future<ImageSegmenter> createImageSegmenter(@NonNull ImageSegmenterModel model, @NonNull ImageSegmenterSettings settings) {
        return createImageSegmenterImpl(model, new ImageSegmenterSettingsInternal(settings.outputConfidenceMasks(), settings.outputCategoryMask(), settings.displayNamesLocale(), settings.runningMode()));
    }

    public Future<InteractiveSegmenter> createInteractiveSegmenter() {
        return createInteractiveSegmenterImpl(InteractiveSegmenterModel.MAGIC_TOUCH_F32_V1, new InteractiveSegmenterSettingsInternal());
    }
    public Future<InteractiveSegmenter> createInteractiveSegmenter(@NonNull InteractiveSegmenterModel model) {
        return createInteractiveSegmenterImpl(model, new InteractiveSegmenterSettingsInternal());
    }
    public Future<InteractiveSegmenter> createInteractiveSegmenter(@NonNull InteractiveSegmenterModel model, @NonNull InteractiveSegmenterSettings settings) {
        return createInteractiveSegmenterImpl(model, new InteractiveSegmenterSettingsInternal(settings.outputConfidenceMasks(), settings.outputCategoryMask(), settings.runningMode()));
    }

    public Future<ObjectDetector> createObjectDetector() {
        return createObjectDetectorImpl(ObjectDetectorModel.EFFICIENTDET_LITE0_F32_V1, new ObjectDetectorSettingsInternal());
    }
    public Future<ObjectDetector> createObjectDetector(@NonNull ObjectDetectorModel model) {
        return createObjectDetectorImpl(model, new ObjectDetectorSettingsInternal());
    }
    public Future<ObjectDetector> createObjectDetector(@NonNull ObjectDetectorModel model, @NonNull ObjectDetectorSettings settings) {
        return createObjectDetectorImpl(model, new ObjectDetectorSettingsInternal(settings.displayNamesLocale(), settings.maxResults(), settings.scoreThreshold(), settings.categoryAllowlist(), settings.categoryDenylist(), settings.runningMode()));
    }

    public Future<PoseLandmarker> createPoseLandmarker() {
        return createPoseLandmarkerImpl(PoseLandmarkerModel.POSE_LANDMARKER_LITE_F32_V1, new PoseLandmarkerSettingsInternal());
    }
    public Future<PoseLandmarker> createPoseLandmarker(@NonNull PoseLandmarkerModel model) {
        return createPoseLandmarkerImpl(model, new PoseLandmarkerSettingsInternal());
    }
    public Future<PoseLandmarker> createPoseLandmarker(@NonNull PoseLandmarkerModel model, @NonNull PoseLandmarkerSettings settings) {
        return createPoseLandmarkerImpl(model, new PoseLandmarkerSettingsInternal(settings.minPoseDetectionConfidence(), settings.minPosePresenceConfidence(), settings.minTrackingConfidence(), settings.numPoses(), settings.outputSegmentationMasks(), settings.runningMode()));
    }
    //endregion
}
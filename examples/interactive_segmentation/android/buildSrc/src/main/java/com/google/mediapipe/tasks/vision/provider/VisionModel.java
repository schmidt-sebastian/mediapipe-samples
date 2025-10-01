package com.google.mediapipe.tasks.vision.provider;

public class VisionModel {
    private final String enumName;
    private final String modelName;
    private final String version;
    private final String quantization;

    public VisionModel(String enumName, String modelName, String version, String quantization) {
        this.enumName = enumName;
        this.modelName = modelName;
        this.version = version;
        this.quantization = quantization;
    }

    public VisionModel(String enumName, String modelName, String version) {
        this(enumName, modelName, version, "FLOAT32"); // Default quantization to FLOAT32
    }

    public String getEnumName() {
        return enumName;
    }

    public String getModelName() {
        return modelName;
    }

    public String getVersion() {
        return version;
    }

    public String getQuantization() {
        return quantization;
    }
}
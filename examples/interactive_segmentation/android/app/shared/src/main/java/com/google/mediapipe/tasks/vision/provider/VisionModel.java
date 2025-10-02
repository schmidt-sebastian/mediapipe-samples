package com.google.mediapipe.tasks.vision.provider;

import com.google.mediapipe.tasks.core.Quantization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VisionModel {

    private final String enumName;
    private final String modelName;
    private final String version;
    private final Quantization quantization;


    public VisionModel(String enumName, String modelName, String version, Quantization quantization) {
        this.enumName = enumName;
        this.modelName = modelName;
        this.version = version;
        this.quantization = quantization;
    }

    /**
     * Transforms a model name string (such as face_landmarker_with_blendshapes_v2_fp32) into a {@link
     * VisionModel}.
     */
    public static VisionModel fromCanonicalName(String name) {
        Pattern pattern = Pattern.compile("^(.*)_(v[0-9]+)_(fp16|fp32|int8)$");
        Matcher matcher = pattern.matcher(name);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unsupported model name: " + name);
        }
        String modelName = matcher.group(1);
        String version = matcher.group(2);
        String quantizationString = matcher.group(3);
        Quantization quantization = Quantization.fromCanonicalName(quantizationString);
        return new VisionModel(name.toUpperCase(), modelName, version, quantization);
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

    public Quantization getQuantization() {
        return quantization;
    }
}
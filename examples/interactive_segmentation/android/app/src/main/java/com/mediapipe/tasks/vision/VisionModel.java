package com.mediapipe.tasks.vision;

import com.mediapipe.tasks.core.Quantization;

public interface VisionModel {
    // You can add common properties here, like the model name or version
    String getModelName();

    String getVersion();

    Quantization getQuantization();
}

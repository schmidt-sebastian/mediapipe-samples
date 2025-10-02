package com.google.mediapipe.tasks.vision.provider;

import com.google.mediapipe.tasks.core.Quantization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface VisionModel {


     String getEnumName() ;
     String getModelName();

     String getVersion();

    Quantization getQuantization();
}
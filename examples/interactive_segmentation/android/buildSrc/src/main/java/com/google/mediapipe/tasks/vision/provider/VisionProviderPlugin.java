package com.google.mediapipe.tasks.vision.provider;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class VisionProviderPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().register("generateVisionProvider", GenerateVisionProviderTask.class);
    }
}
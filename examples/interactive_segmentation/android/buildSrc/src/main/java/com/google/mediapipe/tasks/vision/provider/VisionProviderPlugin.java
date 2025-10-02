package com.google.mediapipe.tasks.vision.provider;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class VisionProviderPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().register("generateVisionProvider", GenerateVisionProviderTask.class);

    }
}
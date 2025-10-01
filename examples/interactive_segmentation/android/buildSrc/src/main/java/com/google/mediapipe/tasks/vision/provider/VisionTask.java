package com.google.mediapipe.tasks.vision.provider;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import javax.inject.Inject;

/**
 * Represents a single vision task configuration.
 */
public abstract class VisionTask {

    private final String name;

    @Inject
    public VisionTask(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the task (e.g., "FaceDetector").
     * @return The task name.
     */
    @Input
    public String getName() {
        return name;
    }

//    /**
//     * A Gradle Property that will hold the enum name of the default model
//     * for this task's parameter-less create() method.
//     * @return The Property for the default model name.
//     */
//    public abstract Property<String> getDefaultModel();
//
//    /**
//     * The list of model configurations to be included in the generated enum.
//     * By making this abstract, you let Gradle provide the ListProperty implementation.
//     * @return The ListProperty for the models.
//     */
//    public abstract ListProperty<VisionModel> getModels();
}

package com.google.mediapipe.npu;

import org.gradle.api.tasks.Input;

/**
 * Data class to structure the model information for the task configuration.
 * This makes the Gradle DSL cleaner and more type-safe.
 */
public abstract class ModelSpec {
    @Input
    public abstract String getTask();

    @Input
    public abstract String getModel();
}
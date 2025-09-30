package com.google.mediapipe.npu;

import org.gradle.api.provider.Property;

/**
 * Defines the configurable block for our plugin.
 */
public abstract class CodeGenExtension {

    /**
     * The package name for the generated file.
     */
    public abstract Property<String> getPackageName();

    /**
     * The class name for the generated file.
     */
    public abstract Property<String> getClassName();
}
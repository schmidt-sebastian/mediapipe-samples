package com.google.mediapipe.tasks.vision.provider;

import com.squareup.kotlinpoet.FileSpec;
import org.gradle.api.DefaultTask;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;

/**
 * The main Gradle task that orchestrates the generation of the VisionProvider class
 * and the associated AI Pack modules.
 */
public abstract class GenerateVisionProviderTask extends DefaultTask {

    private final NamedDomainObjectContainer<VisionTask> tasksConfiguration;

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @OutputDirectory
    @Optional
    public abstract DirectoryProperty getAipackModulesDir();

    @Inject
    public GenerateVisionProviderTask(ObjectFactory objectFactory) {
        this.tasksConfiguration = objectFactory.domainObjectContainer(VisionTask.class);
    }

    @Nested
    public NamedDomainObjectContainer<VisionTask> getTasksConfiguration() {
        return tasksConfiguration;
    }

    @TaskAction
    public void generate() throws IOException {
        // Delegate code generation to the helper class
        VisionProviderCodeGenerator codeGenerator = new VisionProviderCodeGenerator(getTasksConfiguration());
        FileSpec fileSpec = codeGenerator.generateVisionProviderFile();

        // Write the generated Kotlin file
        System.err.println("Output dir for generated code: " + getOutputDir().get().getAsFile());
        getOutputDir().get().getAsFile().mkdirs();
        fileSpec.writeTo(getOutputDir().get().getAsFile());

        // Delegate AI Pack module generation if the directory is present
        if (getAipackModulesDir().isPresent()) {
            AIPackModuleGenerator aipackGenerator =
                    new AIPackModuleGenerator(getTasksConfiguration(), getAipackModulesDir().get().getAsFile());
            aipackGenerator.generate();
        }
    }
}
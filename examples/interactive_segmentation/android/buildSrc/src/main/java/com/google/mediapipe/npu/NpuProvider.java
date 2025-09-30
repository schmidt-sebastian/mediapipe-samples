package com.google.mediapipe.npu;

import com.android.build.api.dsl.ApplicationExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public class NpuProvider implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        // Note: Assumes CodeGenExtension and GenerateCodeTask classes are defined elsewhere.
        CodeGenExtension extension = project.getExtensions().create("buildInfo", CodeGenExtension.class);

        TaskProvider<GenerateCodeTask> generateCodeTask = project.getTasks().register("generateBuildInfo", GenerateCodeTask.class, task -> {
            task.getPackageName().set("com.google.mediapipe.npu");
            task.getClassName().set("NpuLoader");
            task.getOutputDir().set(project.getLayout().getBuildDirectory().dir("generated/source/buildInfo/java"));
        });

        // The original code uses a different task name, but the class name implies this task.
        // I have used the class name from your previous example.
        project.getTasks().register("createNpuFeatureModule", CreateNpuModuleTask.class);

        project.afterEvaluate(p -> {
            // Get the Android application extension to configure source sets.
            ApplicationExtension androidExtension = p.getExtensions().getByType(ApplicationExtension.class);

            androidExtension.getSourceSets().getByName("main", sourceSet -> {
//                // Access the Kotlin source set extension to add the generated source directory.
//                KotlinSourceSet kotlinSourceSet = (KotlinSourceSet) sourceSet.getExtensions().getByName("kotlin");
//                kotlinSourceSet.srcDir(generateCodeTask.get().getOutputDir());
            });

            // This is an unusual pattern that executes a task method during the configuration phase.
            // It is preserved here to match the original Kotlin code's behavior.
            generateCodeTask.get().generate();
        });
    }
}
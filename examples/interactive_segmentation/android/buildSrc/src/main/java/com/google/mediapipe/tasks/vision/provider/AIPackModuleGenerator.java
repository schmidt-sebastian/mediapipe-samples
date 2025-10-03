package com.google.mediapipe.tasks.vision.provider;

import org.gradle.api.NamedDomainObjectContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Handles the generation of AI Pack modules, including downloading models
 * and creating build scripts.
 */
class AIPackModuleGenerator {
    private static final String MEDIAPIPE_GCS_URL_BASE = "https://storage.googleapis.com/mediapipe-tasks/vision/";
    private final NamedDomainObjectContainer<VisionTask> tasksConfiguration;
    private final File aipackModulesDir;

    public AIPackModuleGenerator(NamedDomainObjectContainer<VisionTask> tasksConfiguration, File aipackModulesDir) {
        this.tasksConfiguration = tasksConfiguration;
        this.aipackModulesDir = aipackModulesDir;
    }

    public void generate() throws IOException {
        System.err.println("Output dir for AI Pack modules: " + aipackModulesDir);
        aipackModulesDir.mkdirs();

        for (VisionTask task : tasksConfiguration) {
            for (String modelName : task.getModels().get()) {
                VisionModel visionModel = VisionModel.fromCanonicalName(task.name, modelName);
                String moduleDirName = "aipack-" + modelName.toLowerCase().replace("_", "-");
                File moduleDir = new File(aipackModulesDir, moduleDirName);

                // Create the AI Pack directory structure
                File assetsDir = new File(moduleDir, "src/main/assets");
                assetsDir.mkdirs();

                // Download the model file directly into the assets folder
                String modelFileName = modelName + ".tflite";
                URL modelUrl = new URL(visionModel.createModelUrl());
                Path destinationPath = new File(assetsDir, modelFileName).toPath();
                if (!Files.exists(destinationPath)) {
                    System.out.println("Downloading " + modelUrl + " to " + destinationPath);
                    try (InputStream in = modelUrl.openStream()) {
                        Files.copy(in, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println("Failed to download model: " + modelName);
                        throw e;
                    }
                }

                // Generate the build.gradle.kts for the AI Pack
                Path buildScriptPath = moduleDir.toPath().resolve("build.gradle.kts");
                String buildScriptContent = generateAIPackBuildScript(moduleDirName);
                Files.writeString(buildScriptPath, buildScriptContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        }
    }

    private String generateAIPackBuildScript(String packName) {
        return "plugins {\n" +
                "    id(\"com.android.ai-pack\")\n" +
                "}\n\n" +
                "aiPack {\n" +
                "    packName = \"" + packName + "\"\n" +
                "    dynamicDelivery {\n" +
                "        deliveryType = \"on-demand\"\n" +
                "    }\n" +
                "}\n";
    }
}
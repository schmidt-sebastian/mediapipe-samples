package com.google.mediapipe.npu;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * A Gradle task to create supporting Android library modules for MediaPipe.
 * <p>
 * This can be used for two purposes:
 * 1.  <b>NPU Acceleration</b>: Creates modules containing pre-compiled Qualcomm Hexagon
 * delegate libraries for hardware acceleration.
 * 2.  <b>AI Pack Assets</b>: Creates a module that downloads and bundles specified
 * .tflite models from the official MediaPipe model repository.
 */
public class CreateNpuModuleTask extends DefaultTask {

    // --- Inputs for Qualcomm NPU Modules ---
    private final Property<String> qualcommLibDirectory;

    // --- Inputs for AI Pack (Asset) Modules ---
    private final ListProperty<ModelSpec> aiPackModels;
    private final Property<String> aiPackModuleName;

    @Inject
    public CreateNpuModuleTask() {
        this.qualcommLibDirectory = getProject().getObjects().property(String.class);
        this.aiPackModels = getProject().getObjects().listProperty(ModelSpec.class);
        this.aiPackModuleName = getProject().getObjects().property(String.class);
    }

    @Input
    @Optional
    public Property<String> getQualcommLibDirectory() {
        return qualcommLibDirectory;
    }

    /**
     * Sets the path to the Qualcomm library directory.
     * This setter allows direct assignment in build.gradle.kts.
     */
    public void setQualcommLibDirectory(String path) {
        this.qualcommLibDirectory.set(path);
    }

    @Nested
    @Optional
    public ListProperty<ModelSpec> getAiPackModels() {
        return aiPackModels;
    }

    @Input
    @Optional
    public Property<String> getAiPackModuleName() {
        return aiPackModuleName;
    }

    @TaskAction
    public void createModules() {
        boolean didRun = false;

        // Run the NPU setup if the library directory is provided.
        if (qualcommLibDirectory.isPresent()) {
            setupQualcommModules();
            didRun = true;
        }

        // Run the AI Pack setup if its configuration is present.
        if (aiPackModels.isPresent() && aiPackModuleName.isPresent()) {
            setupAiPackModule();
            didRun = true;
        }

        if (!didRun) {
            System.out.println("⚠️ No inputs provided. Skipping module creation.");
            System.out.println("   Configure 'qualcommLibDirectory' for NPU modules, or");
            System.out.println("   configure 'aiPackModels' and 'aiPackModuleName' for an AI Pack module.");
        }
    }

    /**
     * Sets up a single Android library module for packaging AI assets by downloading them.
     */
    private void setupAiPackModule() {
        final String moduleName = aiPackModuleName.get();
        final File rootDir = getProject().getRootDir();
        final File moduleDir = new File(rootDir, moduleName);

        System.out.println("▶️ Creating AI Pack module: " + moduleName + " at " + moduleDir.getPath());

        // 1. Create directory structure for assets
        File assetsDir = new File(moduleDir, "src/main/assets");
        assetsDir.mkdirs();

        // 2. Download and copy asset files
        List<ModelSpec> modelsToDownload = aiPackModels.get();
        if (modelsToDownload.isEmpty()) {
            System.out.println("✅ Module structure created, but no models were specified in 'aiPackModels'.");
        } else {
            System.out.println("📦 Downloading " + modelsToDownload.size() + " model(s)...");
            modelsToDownload.forEach(spec -> downloadModel(spec, assetsDir));
        }

        // 3. Create build.gradle.kts for the module
        createAiPackBuildFile(moduleDir, moduleName);

        // 4. Create AndroidManifest.xml
        createAndroidManifest(moduleDir, moduleName);

        // 5. Add module to settings.gradle.kts
        addModuleToSettings(rootDir, moduleName);

        System.out.println("🎉 Done with AI Pack module '" + moduleName + "'! Please sync your project.");
        System.out.println("--------------------------------------------------");
    }

    /**
     * Downloads a single model specified by a ModelSpec into the target assets directory.
     */
    private void downloadModel(ModelSpec spec, File assetsDir) {
        String modelFileName = spec.getModel() + ".tflite";
        // Base URL for MediaPipe models
        String modelUrl = "https://storage.googleapis.com/mediapipe-models/" +
                spec.getTask() + "/" + spec.getModel() + "/float32/latest/" + modelFileName;
        File destFile = new File(assetsDir, modelFileName);

        System.out.println("  ➡️ Downloading model for task '" + spec.getTask() + "' from: " + modelUrl);
        try (InputStream input = new URL(modelUrl).openStream()) {
            Files.copy(input, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  ✅ Successfully downloaded to " + destFile.getPath());
        } catch (IOException e) {
            System.out.println("  ❌ ERROR: Failed to download model '" + spec.getModel() + "'. Skipping.");
            System.out.println("     Reason: " + e.getMessage());
        }
    }

    // --- The NPU module creation logic remains largely the same ---

    private void setupQualcommModules() {
        String[] supportedLibs = {
                "hexagon-v66", "hexagon-v68", "hexagon-v69", "hexagon-v73", "hexagon-v75", "hexagon-v79"
        };
        for (String lib : supportedLibs) {
            setupQualcommModuleForLib(lib);
        }
    }

    private void setupQualcommModuleForLib(String lib) {
        final String vendor = "qualcomm";
        final String moduleName = "npu_" + vendor + "_" + lib;
        final File rootDir = getProject().getRootDir();
        final File moduleDir = new File(rootDir, moduleName);

        System.out.println("▶️ Creating NPU module for vendor: " + vendor + ", lib: " + lib + " at " + moduleDir.getPath());

        File jniLibsDir = new File(moduleDir, "src/main/jniLibs/arm64-v8a");
        jniLibsDir.mkdirs();

        File sourceLibDir = new File(qualcommLibDirectory.get(), "/" + lib + "/unsigned");
        if (sourceLibDir.exists() && sourceLibDir.isDirectory()) {
            File[] filesToCopy = sourceLibDir.listFiles();
            if (filesToCopy != null) {
                for (File file : filesToCopy) {
                    try {
                        Files.copy(file.toPath(), new File(jniLibsDir, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.out.println("  ❌ ERROR: Failed to copy " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
            System.out.println("✅ Copied all files from '" + sourceLibDir.getPath() + "' to '" + jniLibsDir.getPath() + "'");
        } else {
            System.out.println("⚠️ WARNING: Source library directory not found at '" + sourceLibDir.getPath() + "'.");
        }

        createNpuBuildFile(moduleDir, moduleName, lib);
        createAndroidManifest(moduleDir, moduleName);
        addModuleToSettings(rootDir, moduleName);

        System.out.println("🎉 Done with NPU module '" + moduleName + "'! Please sync your project.");
        System.out.println("--------------------------------------------------");
    }

    // --- Helper functions are extracted for reuse and clarity ---

    private void createNpuBuildFile(File moduleDir, String moduleName, String lib) {
        String content = "// This file is auto-generated. Do not edit.\n" +
                "plugins { id(\"com.android.library\") }\n" +
                "android {\n" +
                "    namespace = \"com.google.mediapipe.npu." + lib.replace("-", "_") + "\"\n" +
                "    compileSdk = 34\n" +
                "    defaultConfig { minSdk = 24 }\n" +
                "    splits {\n" +
                "        abi {\n" +
                "            isEnable = true\n" +
                "            reset()\n" +
                "            include(\"arm64-v8a\")\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
        try {
            Files.write(new File(moduleDir, "build.gradle.kts").toPath(), content.getBytes(StandardCharsets.UTF_8));
            System.out.println("✅ Created 'build.gradle.kts' for '" + moduleName + "'");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void createAiPackBuildFile(File moduleDir, String moduleName) {
        String content = "// This file is auto-generated. Do not edit.\n" +
                "plugins { id(\"com.android.library\") }\n" +
                "android {\n" +
                "    namespace = \"com.google.mediapipe.aipack." + moduleName.replace("-", "_") + "\"\n" +
                "    compileSdk = 34\n" +
                "    defaultConfig { minSdk = 24 }\n" +
                "}\n";
        try {
            Files.write(new File(moduleDir, "build.gradle.kts").toPath(), content.getBytes(StandardCharsets.UTF_8));
            System.out.println("✅ Created 'build.gradle.kts' for '" + moduleName + "'");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void createAndroidManifest(File moduleDir, String moduleName) {
        File manifestFile = new File(moduleDir, "src/main/AndroidManifest.xml");
        manifestFile.getParentFile().mkdirs();
        String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" />\n";
        try {
            Files.write(manifestFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
            System.out.println("✅ Created 'AndroidManifest.xml' for '" + moduleName + "'");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void addModuleToSettings(File rootDir, String moduleName) {
        File settingsFile = new File(rootDir, "settings.gradle.kts");
        String moduleInclude = "include(\":" + moduleName + "\")";
        try {
            if (settingsFile.exists()) {
                String settingsContent = Files.readString(settingsFile.toPath());
                if (!settingsContent.contains(moduleInclude)) {
                    Files.write(settingsFile.toPath(), ("\n" + moduleInclude + "\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    System.out.println("✅ Added '" + moduleName + "' to settings.gradle.kts");
                }
            }
        } catch (IOException e) {
            System.out.println("  ❌ ERROR: Could not modify settings.gradle.kts: " + e.getMessage());
        }
    }
}
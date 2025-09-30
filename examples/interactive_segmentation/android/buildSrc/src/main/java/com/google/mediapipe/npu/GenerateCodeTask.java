package com.google.mediapipe.npu;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class GenerateCodeTask extends DefaultTask {

    @Input
    public abstract Property<String> getPackageName();

    @Input
    public abstract Property<String> getClassName();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void generate() {
        String targetClassName = getClassName().get();

        // Define ClassNames for external types to handle imports automatically
        ClassName contextCN = ClassName.get("android.content", "Context");
        ClassName logCN = ClassName.get("android.util", "Log");
        ClassName splitInstallManagerCN = ClassName.get("com.google.android.play.core.splitinstall", "SplitInstallManager");
        ClassName splitInstallManagerFactoryCN = ClassName.get("com.google.android.play.core.splitinstall", "SplitInstallManagerFactory");
        ClassName splitInstallRequestCN = ClassName.get("com.google.android.play.core.splitinstall", "SplitInstallRequest");
        ClassName onSuccessListenerCN = ClassName.get("com.google.android.play.core.tasks", "OnSuccessListener");
        ClassName onFailureListenerCN = ClassName.get("com.google.android.play.core.tasks", "OnFailureListener");

        // Build the private `initializeNpu` method
        MethodSpec initializeNpu = MethodSpec.methodBuilder("initializeNpu")
                .addModifiers(Modifier.PRIVATE)
                .beginControlFlow("try")
                .addStatement("$T.loadLibrary($S)", System.class, "npu-driver")
                .addStatement("$T.d(TAG, $S)", logCN, "NPU library loaded successfully.")
                .nextControlFlow("catch ($T e)", UnsatisfiedLinkError.class)
                .addStatement("$T.e(TAG, $S, e)", logCN, "Failed to load NPU library.")
                .endControlFlow()
                .build();

        // Create an anonymous inner class for the success listener
        TypeSpec successListener = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(onSuccessListenerCN)
                .addMethod(MethodSpec.methodBuilder("onSuccess")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Integer.class, "sessionId")
                        .addStatement("$N()", initializeNpu) // Calls the initializeNpu method
                        .build())
                .build();

        // Create an anonymous inner class for the failure listener
        TypeSpec failureListener = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(onFailureListenerCN)
                .addMethod(MethodSpec.methodBuilder("onFailure")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Exception.class, "e")
                        .addStatement("$T.e(TAG, $S, e)", logCN, "Failed to install NPU module")
                        .build())
                .build();

        // Build the private `ensureModuleIsInstalled` method
        MethodSpec ensureModuleIsInstalled = MethodSpec.methodBuilder("ensureModuleIsInstalled")
                .addModifiers(Modifier.PRIVATE)
                .beginControlFlow("if (splitInstallManager.getInstalledModules().contains(moduleName))")
                .addStatement("$T.d(TAG, $S)", logCN, "NPU feature module already installed.")
                .addStatement("$N()", initializeNpu)
                .addStatement("return")
                .endControlFlow()
                .addStatement("$T.d(TAG, $S)", logCN, "NPU feature module not found. Requesting install.")
                .addStatement("$T request = $T.newBuilder().addModule(moduleName).build()", splitInstallRequestCN, splitInstallRequestCN)
                .addStatement("splitInstallManager.startInstall(request).addOnSuccessListener($L).addOnFailureListener($L)",
                        successListener, failureListener)
                .build();

        // Build the main public method
        MethodSpec loadNpuModule = MethodSpec.methodBuilder("loadNpuModule")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(contextCN, "context")
                .addStatement("this.splitInstallManager = $T.create(context)", splitInstallManagerFactoryCN)
                .addStatement("$N()", ensureModuleIsInstalled)
                .build();

        // Build the entire class
        TypeSpec npuLoaderClass = TypeSpec.classBuilder(targetClassName)
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(String.class, "TAG", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$S", targetClassName)
                        .build())
                .addField(FieldSpec.builder(splitInstallManagerCN, "splitInstallManager", Modifier.PRIVATE).build())
                .addField(FieldSpec.builder(String.class, "moduleName", Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("$S", "npu_feature")
                        .build())
                .addMethod(loadNpuModule)
                .addMethod(ensureModuleIsInstalled)
                .addMethod(initializeNpu)
                .build();

        // Create the file spec and write it to the output directory
        JavaFile javaFile = JavaFile.builder(getPackageName().get(), npuLoaderClass)
                .build();

        try {
            javaFile.writeTo(getOutputDir().get().getAsFile());
            System.out.println("✅ Generated " + targetClassName + ".java in " + getOutputDir().get().getAsFile() + " using JavaPoet");
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write generated Java file", e);
        }
    }
}
package com.google.mediapipe.tasks.vision.provider

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class ModuleGeneratorPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        // TODO: Check that android.experimental.enableDeviceTargetingConfigApi=true is set

        AIPackModuleGenerator().generateAndIncludeModules(settings)


    }
}
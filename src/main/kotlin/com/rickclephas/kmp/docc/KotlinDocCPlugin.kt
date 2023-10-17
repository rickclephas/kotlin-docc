package com.rickclephas.kmp.docc

import com.rickclephas.kmp.docc.tasks.DocCConvertTask
import com.rickclephas.kmp.docc.tasks.DocCPreviewTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

public class KotlinDocCPlugin: Plugin<Project> {

    private companion object {
        const val kotlinPluginId = "org.jetbrains.kotlin.multiplatform"
    }

    override fun apply(target: Project) {
        target.pluginManager.withPlugin(kotlinPluginId) {
            val kotlin = target.extensions.getByType(KotlinMultiplatformExtension::class.java)
            kotlin.targets.withType(KotlinNativeTarget::class.java).configureEach { kotlinNativeTarget ->
                kotlinNativeTarget.compilations.getByName("main").kotlinOptions.freeCompilerArgs += "-Xexport-kdoc"
                kotlinNativeTarget.binaries.withType(Framework::class.java).configureEach { framework ->
                    DocCConvertTask.locateOrRegister(framework)
                    DocCPreviewTask.locateOrRegister(framework)
                }
            }
        }
    }
}

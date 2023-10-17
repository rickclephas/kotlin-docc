package com.rickclephas.kmp.docc.tasks.internal

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import javax.inject.Inject

@CacheableTask
@Suppress("LeakingThis")
internal open class CreateDocCSourceBundleTask @Inject constructor(
    @get:Internal
    val target: KotlinNativeTarget
): DefaultTask() {

    internal companion object {
        fun locateOrRegister(target: KotlinNativeTarget): TaskProvider<CreateDocCSourceBundleTask> =
            target.project.tasks.locateOrRegister(
                "createDoccSourceBundle${target.targetName.capitalized()}",
                CreateDocCSourceBundleTask::class.java,
                target
            )
    }

    init {
        onlyIf { HostManager.hostIsMac }
    }

    @get:OutputDirectory
    val outputDirectory: Provider<Directory> = target.sourceBundleDir

    @TaskAction
    fun action() {
        val outputDirectory = outputDirectory.get().asFile
        outputDirectory.apply {
            deleteRecursively()
            mkdirs()
        }
        // TODO: Merge docc files from the source sets
    }
}

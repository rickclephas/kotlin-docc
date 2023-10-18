package com.rickclephas.kmp.docc.tasks.internal

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import javax.inject.Inject

@CacheableTask
@Suppress("LeakingThis")
internal abstract class CreateDocCSourceBundleTask @Inject constructor(
    @get:Internal
    @Transient
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

    private val sourceFiles = project.objects.fileCollection()

    @get:InputFiles
    @PathSensitive(PathSensitivity.ABSOLUTE)
    val source: FileTree = sourceFiles.asFileTree

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @get:OutputDirectory
    val outputDirectory: Provider<Directory> = target.sourceBundleDir

    init {
        onlyIf { HostManager.hostIsMac }
        target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME).allKotlinSourceSets.forAll {
            sourceFiles.from("src/${it.name}/docc")
        }
    }

    @TaskAction
    fun action() {
        fileSystemOperations.sync {
            it.from(source)
            it.into(outputDirectory)
            it.duplicatesStrategy = DuplicatesStrategy.FAIL
        }
    }
}

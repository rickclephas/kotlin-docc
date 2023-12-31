package com.rickclephas.kmp.docc.tasks.internal

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.*
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
        val KotlinNativeTarget.createDoccSourceBundleTask: TaskProvider<CreateDocCSourceBundleTask>
            get() = project.tasks.locateOrRegister(
                "createDoccSourceBundle${taskSuffix}",
                CreateDocCSourceBundleTask::class.java,
                this
            )
    }

    private val sourceFiles = project.objects.fileCollection()

    @get:InputFiles
    @PathSensitive(PathSensitivity.ABSOLUTE)
    val source: FileTree = sourceFiles.asFileTree

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @get:OutputDirectory
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()
        .convention(target.sourceBundleDir)

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

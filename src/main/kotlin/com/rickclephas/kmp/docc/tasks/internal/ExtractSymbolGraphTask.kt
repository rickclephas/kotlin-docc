package com.rickclephas.kmp.docc.tasks.internal

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@DisableCachingByDefault
@Suppress("LeakingThis")
internal abstract class ExtractSymbolGraphTask(
    @get:Internal
    @Transient
    val framework: Framework,
    private val language: String,
): DefaultTask() {

    init {
        onlyIf { HostManager.hostIsMac }
        dependsOn(framework.linkTaskProvider)
    }

    @Internal
    protected val konanTarget = framework.target.konanTarget

    @get:Inject
    @get:Internal
    protected abstract val execOperations: ExecOperations

    @get:Input
    val baseName: Provider<String> = framework.baseNameProvider

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val frameworkDir: Provider<Directory> = framework.linkTaskProvider.flatMap {
        project.layout.dir(it.outputFile)
    }

    @get:OutputDirectory
    val outputDirectory: Provider<Directory> = framework.symbolGraphDir.map { it.dir(language) }

    @TaskAction
    open fun extract() {
        outputDirectory.get().asFile.apply {
            deleteRecursively()
            mkdirs()
        }
    }

    @Internal
    protected fun getSdkPath(): String {
        val sdk = when (konanTarget) {
            is KonanTarget.IOS_ARM64, is KonanTarget.IOS_ARM32 -> "iphoneos"
            is KonanTarget.IOS_SIMULATOR_ARM64, is KonanTarget.IOS_X64 -> "iphonesimulator"
            is KonanTarget.WATCHOS_ARM32, is KonanTarget.WATCHOS_ARM64, is KonanTarget.WATCHOS_DEVICE_ARM64 -> "watchos"
            is KonanTarget.WATCHOS_X64, is KonanTarget.WATCHOS_SIMULATOR_ARM64, is KonanTarget.WATCHOS_X86  -> "watchsimulator"
            is KonanTarget.TVOS_ARM64 -> "appletvos"
            is KonanTarget.TVOS_X64, is KonanTarget.TVOS_SIMULATOR_ARM64 -> "appletvsimulator"
            is KonanTarget.MACOS_X64, is KonanTarget.MACOS_ARM64 -> "macosx"
            else -> error("Unsupported target: ${konanTarget.name}")
        }
        val sdkPathStream = ByteArrayOutputStream()
        execOperations.exec {
            it.commandLine("/usr/bin/xcrun", "--sdk", sdk, "--show-sdk-path")
            it.standardOutput = sdkPathStream
        }
        return sdkPathStream.toString().trim()
    }
}

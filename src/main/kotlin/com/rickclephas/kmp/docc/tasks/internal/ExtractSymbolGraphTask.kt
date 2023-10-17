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
    val framework: Framework,
    private val language: String,
): DefaultTask() {

    init {
        onlyIf { HostManager.hostIsMac }
        dependsOn(framework.linkTaskProvider)
    }

    @get:Inject
    @get:Internal
    protected abstract val execOperations: ExecOperations

    @get:Input
    val baseName: Provider<String> = framework.baseNameProvider

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val frameworkDir: Provider<Directory> = framework.linkTaskProvider.flatMap {
        framework.project.layout.dir(it.outputFile)
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
        val sdk = when (val target = framework.target.konanTarget) {
            KonanTarget.IOS_ARM64, KonanTarget.IOS_ARM32 -> "iphoneos"
            KonanTarget.IOS_SIMULATOR_ARM64, KonanTarget.IOS_X64 -> "iphonesimulator"
            KonanTarget.WATCHOS_ARM32, KonanTarget.WATCHOS_ARM64, KonanTarget.WATCHOS_DEVICE_ARM64 -> "watchos"
            KonanTarget.WATCHOS_X64, KonanTarget.WATCHOS_SIMULATOR_ARM64, KonanTarget.WATCHOS_X86  -> "watchsimulator"
            KonanTarget.TVOS_ARM64 -> "appletvos"
            KonanTarget.TVOS_X64, KonanTarget.TVOS_SIMULATOR_ARM64 -> "appletvsimulator"
            KonanTarget.MACOS_X64, KonanTarget.MACOS_ARM64 -> "macosx"
            else -> error("Unsupported target: ${target.name}")
        }
        val sdkPathStream = ByteArrayOutputStream()
        execOperations.exec {
            it.commandLine("/usr/bin/xcrun", "--sdk", sdk, "--show-sdk-path")
            it.standardOutput = sdkPathStream
        }
        return sdkPathStream.toString().trim()
    }
}

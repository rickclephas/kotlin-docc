package com.rickclephas.kmp.docc.tasks.internal

import org.gradle.api.tasks.*
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.konan.target.KonanTarget
import javax.inject.Inject

@CacheableTask
internal abstract class ExtractSwiftSymbolGraphTask @Inject constructor(
    framework: Framework
): ExtractSymbolGraphTask(framework, "swift") {

    internal companion object {
        fun locateOrRegister(framework: Framework): TaskProvider<ExtractSwiftSymbolGraphTask> =
            framework.project.tasks.locateOrRegister(
                "extractSwiftSymbolGraph${framework.taskSuffix}",
                ExtractSwiftSymbolGraphTask::class.java,
                framework
            )
    }

    override fun extract() {
        super.extract()
        val sdkPath = getSdkPath()
        val target = when (val target = framework.target.konanTarget) {
            KonanTarget.IOS_ARM64 -> "arm64-apple-ios"
            KonanTarget.IOS_ARM32 -> "armv7-apple-ios"
            KonanTarget.IOS_SIMULATOR_ARM64 -> "arm64-apple-ios-simulator"
            KonanTarget.IOS_X64 -> "x86_64-apple-ios-simulator"
            KonanTarget.WATCHOS_ARM32 -> "armv7k-apple-watchos"
            KonanTarget.WATCHOS_ARM64 -> "arm64_32-apple-watchos"
            KonanTarget.WATCHOS_DEVICE_ARM64 -> "arm64-apple-watchos"
            KonanTarget.WATCHOS_X64 -> "x86_64-apple-watchos-simulator"
            KonanTarget.WATCHOS_SIMULATOR_ARM64 -> "arm64-apple-watchos-simulator"
            KonanTarget.WATCHOS_X86 -> "i386-apple-watchos-simulator"
            KonanTarget.TVOS_ARM64 -> "arm64-apple-tvos"
            KonanTarget.TVOS_X64 -> "x86_64-apple-tvos-simulator"
            KonanTarget.TVOS_SIMULATOR_ARM64 -> "arm64-apple-tvos-simulator"
            KonanTarget.MACOS_X64 -> "x86_64-apple-macos"
            KonanTarget.MACOS_ARM64 -> "arm64-apple-macos"
            else -> error("Unsupported target: ${target.name}")
        }
        execOperations.exec { exec ->
            exec.executable = "/usr/bin/xcrun"
            exec.args("swift-symbolgraph-extract",
                "-sdk", sdkPath,
                "-target", target,
                "-F", frameworkDir.get().asFile.parent,
                "-module-name", baseName.get(),
                "-output-dir", outputDirectory.get().asFile.absolutePath,
            )
        }
    }
}

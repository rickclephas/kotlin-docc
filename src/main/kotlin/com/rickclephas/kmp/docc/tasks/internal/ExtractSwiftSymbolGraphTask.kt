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
        val Framework.extractSwiftSymbolGraph: TaskProvider<ExtractSwiftSymbolGraphTask>
            get() = project.tasks.locateOrRegister(
                "extractSwiftSymbolGraph${taskSuffix}",
                ExtractSwiftSymbolGraphTask::class.java,
                this
            )
    }

    override fun extract() {
        super.extract()
        val sdkPath = getSdkPath()
        val target = when (konanTarget) {
            is KonanTarget.IOS_ARM64 -> "arm64-apple-ios"
            is KonanTarget.IOS_ARM32 -> "armv7-apple-ios"
            is KonanTarget.IOS_SIMULATOR_ARM64 -> "arm64-apple-ios-simulator"
            is KonanTarget.IOS_X64 -> "x86_64-apple-ios-simulator"
            is KonanTarget.WATCHOS_ARM32 -> "armv7k-apple-watchos"
            is KonanTarget.WATCHOS_ARM64 -> "arm64_32-apple-watchos"
            is KonanTarget.WATCHOS_DEVICE_ARM64 -> "arm64-apple-watchos"
            is KonanTarget.WATCHOS_X64 -> "x86_64-apple-watchos-simulator"
            is KonanTarget.WATCHOS_SIMULATOR_ARM64 -> "arm64-apple-watchos-simulator"
            is KonanTarget.WATCHOS_X86 -> "i386-apple-watchos-simulator"
            is KonanTarget.TVOS_ARM64 -> "arm64-apple-tvos"
            is KonanTarget.TVOS_X64 -> "x86_64-apple-tvos-simulator"
            is KonanTarget.TVOS_SIMULATOR_ARM64 -> "arm64-apple-tvos-simulator"
            is KonanTarget.MACOS_X64 -> "x86_64-apple-macos"
            is KonanTarget.MACOS_ARM64 -> "arm64-apple-macos"
            else -> error("Unsupported target: ${konanTarget.name}")
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

package com.rickclephas.kmp.docc.tasks.internal

import org.gradle.api.tasks.*
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import javax.inject.Inject

@CacheableTask
internal abstract class ExtractObjCSymbolGraphTask @Inject constructor(
    framework: Framework
): ExtractSymbolGraphTask(framework, "objc") {

    internal companion object {
        fun locateOrRegister(framework: Framework): TaskProvider<ExtractObjCSymbolGraphTask> =
            framework.project.tasks.locateOrRegister(
                "extractObjSymbolGraph${framework.taskSuffix}",
                ExtractObjCSymbolGraphTask::class.java,
                framework
            )
    }

    override fun extract() {
        super.extract()
        val sdkPath = getSdkPath()
        val headersDir = frameworkDir.get().dir("Headers")
        val baseName = baseName.get()
        execOperations.exec { exec ->
            exec.workingDir = headersDir.asFile
            exec.executable = "/usr/bin/xcrun"
            exec.args("clang", "-extract-api",
                "--product-name=$baseName",
                "-o", outputDirectory.get().file("$baseName.symbols.json").asFile.absolutePath,
                "-isysroot", sdkPath,
                "-F", "$sdkPath/System/Library/Frameworks",
                "-I", "./",
                "-x", "objective-c-header",
            )
            headersDir.asFileTree.matching {
                it.include("*.h")
            }.forEach {
                exec.args(it.name)
            }
        }
    }
}

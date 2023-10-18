package com.rickclephas.kmp.docc.tasks.internal

import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

internal val Framework.taskSuffix: String
    get() = "${name.capitalized()}${target.taskSuffix}"

internal val KotlinNativeTarget.taskSuffix: String
    get() = targetName.capitalized()

internal val Framework.baseNameProvider: Provider<String>
    get() = project.provider { baseName.replace('-', '_') }

internal val Framework.symbolGraphDir: Provider<Directory>
    get() = project.layout.buildDirectory.dir("docc/symbol-graphs/${target.targetName}/$name")

internal val Framework.doccArchiveDir: Provider<Directory>
    get() = baseNameProvider.flatMap { baseName ->
        project.layout.buildDirectory.dir("docc/archives/${target.targetName}/$name/$baseName.doccarchive")
    }

internal val KotlinNativeTarget.sourceBundleDir: Provider<Directory>
    get() = project.layout.buildDirectory.dir("docc/source-bundles/${targetName}.docc")

internal fun <T: Task> TaskContainer.locateOrRegister(
    name: String,
    type: Class<T>,
    vararg constructorArgs: Any
): TaskProvider<T> {
    if (names.contains(name)) return named(name, type)
    return register(name, type, *constructorArgs)
}

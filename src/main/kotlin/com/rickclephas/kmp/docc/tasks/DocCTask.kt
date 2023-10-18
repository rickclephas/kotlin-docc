package com.rickclephas.kmp.docc.tasks

import com.rickclephas.kmp.docc.tasks.internal.*
import com.rickclephas.kmp.docc.tasks.internal.CreateDocCSourceBundleTask
import com.rickclephas.kmp.docc.tasks.internal.ExtractObjCSymbolGraphTask
import com.rickclephas.kmp.docc.tasks.internal.ExtractSwiftSymbolGraphTask
import com.rickclephas.kmp.docc.tasks.internal.baseNameProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.konan.target.HostManager
import javax.inject.Inject

@DisableCachingByDefault
@Suppress("LeakingThis")
public abstract class DocCTask(
    @get:Internal
    @Transient
    public val framework: Framework,
    private val subcommand: String
): DefaultTask() {

    init {
        onlyIf { HostManager.hostIsMac }
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        dependsOn(CreateDocCSourceBundleTask.locateOrRegister(framework.target))
        dependsOn(ExtractObjCSymbolGraphTask.locateOrRegister(framework))
        dependsOn(ExtractSwiftSymbolGraphTask.locateOrRegister(framework))
    }

    @get:Inject
    @get:Internal
    protected abstract val execOperations: ExecOperations

    @get:Input
    protected val projectGroup: Provider<String> = project.provider { project.group.toString() }

    @get:Input
    protected val projectVersion: Provider<String> = project.provider { project.version.toString() }

    @get:Input
    public val baseName: Provider<String> = framework.baseNameProvider

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    public val sourceBundle: Provider<Directory> = framework.target.sourceBundleDir

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    public val symbolGraphDir: Provider<Directory> = framework.symbolGraphDir

    @get:OutputDirectory
    public val outputDirectory: Provider<Directory> = framework.doccArchiveDir

    /**
     * Additional DocC arguments that will be provided to the subcommand.
     */
    @get:Input
    public abstract val additionalArgs: ListProperty<String>

    @TaskAction
    public open fun exec() {
        val baseName = baseName.get()
        execOperations.exec {
            it.executable = "/usr/bin/xcrun"
            it.args("docc", subcommand,
                "--additional-symbol-graph-dir", symbolGraphDir.get().asFile.absolutePath,
                "--output-path", outputDirectory.get().asFile.absolutePath,
                "--fallback-display-name", baseName,
                "--fallback-bundle-identifier", "${projectGroup.get()}.$baseName",
                "--fallback-bundle-version", projectVersion.get(),
            )
            it.args(additionalArgs.get())
            it.args(sourceBundle.get().asFile.absolutePath)
        }
    }
}

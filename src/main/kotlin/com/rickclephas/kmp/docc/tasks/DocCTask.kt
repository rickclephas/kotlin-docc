package com.rickclephas.kmp.docc.tasks

import com.rickclephas.kmp.docc.tasks.internal.*
import com.rickclephas.kmp.docc.tasks.internal.CreateDocCSourceBundleTask.Companion.createDoccSourceBundleTask
import com.rickclephas.kmp.docc.tasks.internal.ExtractObjCSymbolGraphTask.Companion.extractObjcSymbolGraphTask
import com.rickclephas.kmp.docc.tasks.internal.ExtractSwiftSymbolGraphTask.Companion.extractSwiftSymbolGraph
import com.rickclephas.kmp.docc.tasks.internal.baseNameProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
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
        dependsOn(framework.extractObjcSymbolGraphTask)
        dependsOn(framework.extractSwiftSymbolGraph)
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

    /**
     * The `.docc` source bundle used to build the `.doccarchive`.
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    public val sourceBundle: DirectoryProperty = project.objects.directoryProperty()
        .convention(framework.target.createDoccSourceBundleTask.flatMap { it.outputDirectory })

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    public val symbolGraphDir: Provider<Directory> = framework.symbolGraphDir

    /**
     * The `.doccarchive` directory.
     */
    @get:OutputDirectory
    public val outputDirectory: DirectoryProperty = project.objects.directoryProperty()
        .convention(framework.doccArchiveDir)

    /**
     * The display name passed to `--fallback-display-name`, defaults to the `baseName`.
     */
    @get:Input
    public val displayName: Property<String> = project.objects.property(String::class.java)
        .convention(baseName)

    /**
     * The bundle identifier passed to `--bundle-identifier` and `--fallback-bundle-identifier`,
     * defaults to `${project.group}.$baseName`.
     */
    @get:Input
    public val bundleIdentifier: Property<String> = project.objects.property(String::class.java)
        .convention(baseName.flatMap { baseName -> projectGroup.map { "$it.$baseName" } })

    /**
     * The bundle version passed to `--fallback-bundle-version`, defaults to `${project.version}`.
     */
    @get:Input
    public val bundleVersion: Property<String> = project.objects.property(String::class.java)
        .convention(projectVersion)

    /**
     * Additional DocC arguments that will be provided to the subcommand.
     */
    @get:Input
    public val additionalArgs: ListProperty<String> = project.objects.listProperty(String::class.java)

    @TaskAction
    public open fun exec() {
        execOperations.exec {
            it.executable = "/usr/bin/xcrun"
            it.args("docc", subcommand,
                "--additional-symbol-graph-dir", symbolGraphDir.get().asFile.absolutePath,
                "--output-path", outputDirectory.get().asFile.absolutePath,
                "--fallback-display-name", displayName.get(),
                "--fallback-bundle-identifier", bundleIdentifier.get(),
                "--fallback-bundle-version", bundleVersion.get(),
            )
            it.args(additionalArgs.get())
            it.args(sourceBundle.get().asFile.absolutePath)
        }
    }
}

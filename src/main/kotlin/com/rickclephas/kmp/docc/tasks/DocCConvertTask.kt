package com.rickclephas.kmp.docc.tasks

import com.rickclephas.kmp.docc.tasks.internal.locateOrRegister
import com.rickclephas.kmp.docc.tasks.internal.taskSuffix
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import javax.inject.Inject

@CacheableTask
public abstract class DocCConvertTask @Inject constructor(
    framework: Framework
): DocCTask(framework, "convert") {

    internal companion object {
        val Framework.doccConvertTask: TaskProvider<DocCConvertTask>
            get() = project.tasks.locateOrRegister(
                "doccConvert${taskSuffix}",
                DocCConvertTask::class.java,
                this
            )
    }

    init {
        description = "Generates a .doccarchive for framework '${framework.name}' with target '${framework.target.targetName}'"
    }

    /**
     * Indicates if an index should be created for the produced `.doccarchive`, defaults to `false`.
     */
    @get:Input
    public val createIndex: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)

    override fun exec() {
        super.exec()
        if (!createIndex.get()) return
        execOperations.exec {
            it.executable = "/usr/bin/xcrun"
            it.args("docc", "process-archive", "index",
                outputDirectory.get().asFile.absolutePath,
                "--bundle-identifier", bundleIdentifier.get(),
            )
        }
    }
}

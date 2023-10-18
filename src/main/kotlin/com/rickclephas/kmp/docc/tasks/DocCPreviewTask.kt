package com.rickclephas.kmp.docc.tasks

import com.rickclephas.kmp.docc.tasks.internal.locateOrRegister
import com.rickclephas.kmp.docc.tasks.internal.taskSuffix
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import javax.inject.Inject

@DisableCachingByDefault
public abstract class DocCPreviewTask @Inject constructor(
    framework: Framework
): DocCTask(framework, "preview") {

    internal companion object {
        fun locateOrRegister(framework: Framework): TaskProvider<DocCPreviewTask> =
            framework.project.tasks.locateOrRegister(
                "doccPreview${framework.taskSuffix}",
                DocCPreviewTask::class.java,
                framework
            )
    }

    /**
     * Port number passed to `--port`, defaults to `8080`.
     */
    @get:Input
    public val port: Property<Int> = project.objects.property(Int::class.java).convention(8080)

    init {
        description = "Previews DocC documentation for framework '${framework.name}' with target '${framework.target.targetName}'"
        additionalArgs.addAll(port.map { listOf("--port", it.toString()) })
    }
}

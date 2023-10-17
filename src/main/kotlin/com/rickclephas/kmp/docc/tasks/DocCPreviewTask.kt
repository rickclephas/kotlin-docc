package com.rickclephas.kmp.docc.tasks

import com.rickclephas.kmp.docc.tasks.internal.locateOrRegister
import com.rickclephas.kmp.docc.tasks.internal.taskSuffix
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

    init {
        description = "Previews DocC documentation for framework '${framework.name}' with target '${framework.target.targetName}'"
    }
}

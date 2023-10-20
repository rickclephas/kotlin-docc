package com.rickclephas.kmp.docc.dsl

import com.rickclephas.kmp.docc.tasks.DocCConvertTask
import com.rickclephas.kmp.docc.tasks.DocCConvertTask.Companion.doccConvertTask
import com.rickclephas.kmp.docc.tasks.DocCPreviewTask
import com.rickclephas.kmp.docc.tasks.DocCPreviewTask.Companion.doccPreviewTask
import com.rickclephas.kmp.docc.tasks.DocCTask
import org.gradle.api.Action
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

public class DocCTasks internal constructor(framework: Framework) {

    internal val convertTask = framework.doccConvertTask
    public fun convert(configure: DocCConvertTask.() -> Unit): Unit = convertTask.configure { it.configure() }
    public fun convert(configure: Action<DocCConvertTask>): Unit = convert { configure.execute(this) }

    internal val previewTask = framework.doccPreviewTask
    public fun preview(configure: DocCPreviewTask.() -> Unit): Unit = previewTask.configure { it.configure() }
    public fun preview(configure: Action<DocCPreviewTask>): Unit = preview { configure.execute(this) }

    public fun common(configure: DocCTask.() -> Unit) {
        convert(configure)
        preview(configure)
    }
    public fun common(configure: Action<DocCTask>) {
        convert { configure.execute(this) }
        preview { configure.execute(this) }
    }
}

public val Framework.docC: DocCTasks get() = DocCTasks(this)
public fun Framework.docC(configure: DocCTasks.() -> Unit): DocCTasks = docC.apply(configure)
public fun Framework.docC(configure: Action<DocCTasks>): DocCTasks = docC { configure.execute(this) }

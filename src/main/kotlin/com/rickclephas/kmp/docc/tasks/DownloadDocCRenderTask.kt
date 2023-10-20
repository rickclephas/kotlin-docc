package com.rickclephas.kmp.docc.tasks

import com.rickclephas.kmp.docc.tasks.internal.GetKotlinDocCRenderUrlTask.Companion.getKotlinDoccRenderUrlTask
import com.rickclephas.kmp.docc.tasks.internal.locateOrRegister
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.net.URL
import java.nio.channels.Channels
import javax.inject.Inject

@CacheableTask
public abstract class DownloadDocCRenderTask: DefaultTask() {

    internal companion object {
        val Project.downloadDoccRenderTask: TaskProvider<DownloadDocCRenderTask>
            get() = tasks.locateOrRegister("downloadDoccRender", DownloadDocCRenderTask::class.java)
    }

    @get:Inject
    protected abstract val fileSystemOperations: FileSystemOperations

    /**
     * The URL of the DocC-Render distribution ZIP that should be downloaded.
     */
    @get:Input
    public val downloadUrl: Property<String> = project.objects.property(String::class.java)
        .convention(project.getKotlinDoccRenderUrlTask.flatMap { it.outputFile }.map { it.asFile.readText() })

    private val downloadFile = project.layout.buildDirectory.file("docc/render.zip")

    /**
     * The directory where the DoC-Render distribution should be stored.
     */
    @get:OutputDirectory
    public val outputDirectory: DirectoryProperty = project.objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("docc/render"))

    @TaskAction
    public fun download() {
        val downloadUrl = URL(downloadUrl.get())
        val downloadFile = downloadFile.get().asFile
        downloadFile.outputStream().channel.transferFrom(Channels.newChannel(downloadUrl.openStream()), 0, Long.MAX_VALUE)
        fileSystemOperations.sync {
            it.from(project.zipTree(downloadFile))
            it.into(outputDirectory.get())
        }
        downloadFile.delete()
    }
}

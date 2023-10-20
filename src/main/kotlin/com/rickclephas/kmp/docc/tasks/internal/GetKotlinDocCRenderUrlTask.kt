package com.rickclephas.kmp.docc.tasks.internal

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@CacheableTask
internal abstract class GetKotlinDocCRenderUrlTask: DefaultTask() {

    companion object {
        val Project.getKotlinDoccRenderUrlTask: TaskProvider<GetKotlinDocCRenderUrlTask>
            get() = tasks.locateOrRegister("getKotlinDoccRenderUrl", GetKotlinDocCRenderUrlTask::class.java)
    }

    abstract class CacheKeyValueSource: ValueSource<String, ValueSourceParameters.None> {
        override fun obtain(): String = DateTimeFormatter.ISO_LOCAL_DATE
            .withZone(ZoneId.of("UTC")).format(Instant.now())
    }

    @get:Input
    @Suppress("unused")
    val cacheKey: Provider<String> = project.providers.of(CacheKeyValueSource::class.java) {}

    @get:OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty()
        .convention(project.layout.buildDirectory.file("docc/kotlin-docc-render-url.txt"))

    @TaskAction
    fun getUrl() {
        val outputFile = outputFile.get().asFile
        try {
            val releasesUrl = "https://github.com/rickclephas/kotlin-docc-render/releases"
            val urlConnection = URL("$releasesUrl/latest").openConnection() as HttpURLConnection
            urlConnection.instanceFollowRedirects = false
            val version = urlConnection.getHeaderField("Location").split('/').last()
            outputFile.writeText("$releasesUrl/download/$version/kotlin-docc-render.zip")
        } catch (e: Exception) {
            if (outputFile.exists()) {
                logger.warn("Failed to get Kotlin-DocC-Render URL, using cache instead.")
            } else {
                throw IllegalStateException("Failed to get Kotlin-DocC-Render URL", e)
            }
        }
    }
}

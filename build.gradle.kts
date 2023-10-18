@file:Suppress("UnstableApiUsage")

import java.util.Properties

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    signing
}

group = "com.rickclephas.kmp"
version = "1.0-SNAPSHOT"

kotlin {
    explicitApi()
    jvmToolchain(11)
}

java {
    withJavadocJar()
    withSourcesJar()
}

gradlePlugin {
    website = "https://github.com/rickclephas/kotlin-docc"
    vcsUrl = "https://github.com/rickclephas/kotlin-docc"
    plugins {
        create("kotlin-docc") {
            id = "com.rickclephas.kmp.docc"
            displayName = "kotlin-docc"
            description = "Swift DocC documentation for Kotlin Multiplatform frameworks"
            implementationClass = "com.rickclephas.kmp.docc.KotlinDocCPlugin"
            tags = listOf("kotlin", "swift", "documentation")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    testImplementation(kotlin("test"))
}

//region Publishing

ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKey"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null
val localPropsFile = project.rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localPropsFile.reader()
        .use { Properties().apply { load(it) } }
        .onEach { (name, value) -> ext[name.toString()] = value }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKey"] = System.getenv("SIGNING_SECRET_KEY")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

fun getExtraString(name: String) = ext[name]?.toString()

val signPublications = getExtraString("signing.keyId") != null

publishing {
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }

    publications.withType<MavenPublication> {
        if (signPublications) signing.sign(this)

        pom {
            name = "kotlin-docc"
            description = "Swift DocC documentation for Kotlin Multiplatform frameworks"
            url = "https://github.com/rickclephas/kotlin-docc"
            licenses {
                license {
                    name = "MIT"
                    url = "https://opensource.org/licenses/MIT"
                }
            }
            developers {
                developer {
                    id = "rickclephas"
                    name = "Rick Clephas"
                    email = "rclephas@gmail.com"
                }
            }
            scm {
                url = "https://github.com/rickclephas/kotlin-docc"
            }
        }
    }
}

if (signPublications) {
    signing {
        getExtraString("signing.secretKey")?.let { secretKey ->
            useInMemoryPgpKeys(getExtraString("signing.keyId"), secretKey, getExtraString("signing.password"))
        }
    }
}

//endregion

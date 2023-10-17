plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
}

group = "com.rickclephas.kmp"
version = "1.0-SNAPSHOT"

kotlin {
    explicitApi()
    jvmToolchain(11)
}

gradlePlugin {
    plugins {
        create("kotlin-docc") {
            id = "com.rickclephas.kmp.docc"
            implementationClass = "com.rickclephas.kmp.docc.KotlinDocCPlugin"
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

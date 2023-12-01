plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.publish)
}

group = "cn.numeron"
version = libs.versions.discovery.get()

kotlin {
    jvmToolchain(11)
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation(libs.android.gradle)
    implementation(libs.ow2.asm.tree)
    implementation(libs.ow2.asm.commons)
}

gradlePlugin {
    website.set("https://github.com/xiazunyang/discovery")
    vcsUrl.set("https://github.com/xiazunyang/discovery.git")
    plugins {
        create("discovery") {
            id = "cn.numeron.discovery"
            displayName = "Discovery Plugin"
            description = "An android gradle plugin, helps android developers in multi-module projects to get their inaccessible instances from accessible abstract classes."
            tags.set(listOf("discovery", "android", "router", "transform"))
            implementationClass = "cn.numeron.discovery.DiscoveryPlugin"
        }
    }
}
package cn.numeron.discovery.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class DiscoveryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val androidAppExtension = target.extensions.findByType(AppExtension::class.java)
        val androidLibExtension = target.extensions.findByType(LibraryExtension::class.java)
        if (androidAppExtension != null || androidLibExtension != null) {
            val discoveryTransform = DiscoveryTransform(target)
            androidAppExtension?.registerTransform(discoveryTransform)
            androidLibExtension?.registerTransform(discoveryTransform)
        }
    }

}
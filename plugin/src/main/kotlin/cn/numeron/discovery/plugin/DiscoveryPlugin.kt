package cn.numeron.discovery.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class DiscoveryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val androidAppExtension = target.extensions.findByType(AppExtension::class.java)
        if (androidAppExtension != null) {
            //创建并注册Transform
            val discoveryTransform = DiscoveryTransform(target)
            androidAppExtension.registerTransform(discoveryTransform)
        }
    }

}
package cn.numeron.discovery.plugin

import cn.numeron.discovery.core.DiscoveryConfig
import cn.numeron.discovery.core.DiscoveryCore
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class DiscoveryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val androidAppExtension = target.extensions.findByType(AppExtension::class.java)
        val androidLibExtension = target.extensions.findByType(LibraryExtension::class.java)
        if (androidAppExtension != null || androidLibExtension != null) {
            //创建并注册Transform
            val discoveryTransform = DiscoveryTransform(target)
            androidAppExtension?.registerTransform(discoveryTransform)
            androidLibExtension?.registerTransform(discoveryTransform)
            //创建discovery的配置
            target.extensions.create("discovery", DiscoveryConfig::class.java)
            target.afterEvaluate {
                //并在该模块评估过后，加载配置并保存到根目录下
                val discoveryConfig = it.extensions.findByType(DiscoveryConfig::class.java)
                if (discoveryConfig != null) {
                    DiscoveryCore.saveConfig(discoveryConfig)
                }
            }
        }
    }

}
package cn.numeron.discovery.kcp

import cn.numeron.discovery.core.DiscoveryConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

class DiscoveryGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("discovery", DiscoveryConfig::class.java)
    }
}
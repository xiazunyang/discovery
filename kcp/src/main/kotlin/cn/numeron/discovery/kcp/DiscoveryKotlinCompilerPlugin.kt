package cn.numeron.discovery.kcp

import cn.numeron.discovery.core.DiscoveryConfig
import cn.numeron.discovery.core.DiscoveryCore
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation

class DiscoveryKotlinCompilerPlugin : KotlinCompilerPluginSupportPlugin {

    private val pluginArtifact = SubpluginArtifact(NUMERON_GROUP, NUMERON_ARTIFACT)

    override fun getCompilerPluginId(): String = "$NUMERON_GROUP.$NUMERON_ARTIFACT"

    override fun getPluginArtifact(): SubpluginArtifact = pluginArtifact

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return kotlinCompilation is KotlinJvmAndroidCompilation
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val projectName = project.name
        val rootProjectBuildDir = project.rootProject.buildDir.absolutePath
        return project.provider {
            listOf(
                SubpluginOption(DiscoveryCore.PROJECT_NAME, projectName),
                SubpluginOption(DiscoveryCore.ROOT_PROJECT_BUILD_DIR, rootProjectBuildDir)
            )
        }
    }

    override fun apply(target: Project) {
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
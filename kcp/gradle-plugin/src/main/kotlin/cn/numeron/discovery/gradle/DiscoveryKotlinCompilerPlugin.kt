package cn.numeron.discovery.gradle

import com.google.auto.service.AutoService
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation

@AutoService(KotlinCompilerPluginSupportPlugin::class)
class DiscoveryKotlinCompilerPlugin : KotlinCompilerPluginSupportPlugin {

    private val pluginArtifact = SubpluginArtifact(
        artifactId = BuildConfig.ARTIFACT_ID,
        groupId = BuildConfig.GROUP_ID,
        version = BuildConfig.VERSION
    )

    override fun getPluginArtifact(): SubpluginArtifact = pluginArtifact

    override fun getCompilerPluginId(): String = pluginArtifact.groupId + "." + pluginArtifact.artifactId

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return kotlinCompilation is KotlinJvmAndroidCompilation
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        return kotlinCompilation.target.project.provider {
            listOf()
        }
    }

}
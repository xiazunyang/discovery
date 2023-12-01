package cn.numeron.discovery

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class DiscoveryPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withType(AppPlugin::class.java) {
            // 为项目添加discovery依赖
            project.dependencies.add("implementation", "$DISCOVERY_GROUP:$DISCOVERY_LIBRARY:$DISCOVERY_VERSION")
            // 注册DiscoveryTask
            val appComponents = project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            appComponents.onVariants { variant ->
                val taskProvider = project.tasks.register("${variant.name}Discovery", DiscoveryTask::class.java) {
                    it.group = "discovery"
                }
                variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                    .use(taskProvider)
                    .toTransform(
                        type = ScopedArtifact.CLASSES,
                        inputJars = DiscoveryTask::allJars,
                        inputDirectories = DiscoveryTask::allDirectories,
                        into = DiscoveryTask::output
                    )
            }
        }
    }

    companion object {
        private const val DISCOVERY_GROUP = "cn.numeron"
        private const val DISCOVERY_LIBRARY = "discovery.library"
        private const val DISCOVERY_VERSION = "2.0.0"
    }

}
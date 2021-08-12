package cn.numeron.discovery.kcp

import cn.numeron.discovery.core.DiscoveryCore
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class DiscoveryComponentRegister : ComponentRegistrar {

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        //获取参数
        val projectName = configuration.get(KEY_PROJECT_NAME)
        val rootProjectBuildDir = configuration.get(KEY_ROOT_PROJECT_BUILD_DIR)
        //初始化DiscoveryCore
        DiscoveryCore.init(projectName, rootProjectBuildDir)
        //注册Discovery的Class拦截器
        ClassBuilderInterceptorExtension.registerExtension(project, DiscoveryClassGenerationInterceptor())
    }

}
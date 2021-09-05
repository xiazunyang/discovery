package cn.numeron.discovery.kotlin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(ComponentRegistrar::class)
class DiscoveryComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        //注册Discovery的Class拦截器
        ClassBuilderInterceptorExtension.registerExtension(project, DiscoveryClassGenerationInterceptor())
    }

}
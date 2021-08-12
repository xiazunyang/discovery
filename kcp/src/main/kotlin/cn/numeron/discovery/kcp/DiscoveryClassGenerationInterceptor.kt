package cn.numeron.discovery.kcp

import cn.numeron.discovery.core.DiscoverableImpl
import cn.numeron.discovery.core.DiscoveryCore
import cn.numeron.discovery.core.Modes
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin

class DiscoveryClassGenerationInterceptor : ClassBuilderInterceptorExtension {

    private val discoveryConfig by lazy(DiscoveryCore::loadConfig)
    private val discoverableSet by lazy(DiscoveryCore::loadDiscoverable)
    private val implementationSet by lazy(DiscoveryCore::loadImplementation)

    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink
    ): ClassBuilderFactory = object : ClassBuilderFactory by interceptedFactory {
        override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder {
            println("ClassBuilderFactory newClassBuilder origin = [${origin}]")
            val newClassBuilder = interceptedFactory.newClassBuilder(origin)

            if (discoveryConfig.mode != Modes.Mark) {
                //1.扫描class类，找到Implementation的类

            }

            //2.找到Discoveries类，并修改字节码

            /*return implementationSet
                .groupBy(DiscoverableImpl::discoverableName)
                .toMap()
                .filter {
                    //忽略没有注册过的接口
                    it.key in discoverableSet
                }
                .mapValues {
                    it.value.map(DiscoverableImpl::qualifierName)
                }
                .let {
                    DiscoveryClassBuilder(newClassBuilder, it)
                }*/

            return newClassBuilder
        }


    }

}
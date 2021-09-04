package cn.numeron.discovery.ksp

import cn.numeron.discovery.core.DiscoveryConfig
import cn.numeron.discovery.core.DiscoveryCore
import cn.numeron.discovery.core.Modes
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated

class DiscoveryProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {

    private val discoveryConfig: DiscoveryConfig
    private val discoverableVisitor = DiscoverableVisitor(env)
    private val implementationVisitor = ImplementationVisitor(env)

    init {
        val projectName = env.options[DiscoveryCore.PROJECT_NAME]
        val rootProjectBuildDir = env.options[DiscoveryCore.ROOT_PROJECT_BUILD_DIR]
        DiscoveryCore.init(projectName, rootProjectBuildDir)
        discoveryConfig = DiscoveryCore.loadConfig()
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        //获取所有被Discoverable注解标记的类
        resolver.getSymbolsWithAnnotation(DISCOVERABLE).forEach {
            it.accept(discoverableVisitor, Unit)
        }
        if (discoveryConfig.workMode == Modes.Mark) {
            //如果配置为标记模式，则扫描被Implementation标记的类
            resolver.getSymbolsWithAnnotation(IMPLEMENTATION).forEach {
                it.accept(implementationVisitor, Unit)
            }
        }
        return emptyList()
    }

    override fun finish() {
        DiscoveryCore.saveDiscoverable(discoverableVisitor.discoverableSet)
        println("discovery process finish. found discoverable: ${discoverableVisitor.discoverableSet}")
        env.logger.info("discovery process finish. found discoverable: ${discoverableVisitor.discoverableSet}")
        if (discoveryConfig.workMode == Modes.Mark) {
            DiscoveryCore.saveImplementation(implementationVisitor.implementationSet)
            env.logger.info("discovery processor finish. found implementation: ${implementationVisitor.implementationSet}")
        }
    }

    private companion object {

        private const val DISCOVERABLE = "cn.numeron.discovery.Discoverable"
        private const val IMPLEMENTATION = "cn.numeron.discovery.Implementation"

    }

}
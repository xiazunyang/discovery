package cn.numeron.discovery.ksp

import cn.numeron.discovery.core.DiscoveryCore
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated

class DiscoveryProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {

    private val discoverableSet = mutableSetOf<String>()
    private val visitor = DiscoverableVisitor(discoverableSet, env)

    init {
        val projectName = env.options[DiscoveryCore.PROJECT_NAME]
        val rootProjectBuildDir = env.options[DiscoveryCore.ROOT_PROJECT_BUILD_DIR]
        DiscoveryCore.init(projectName, rootProjectBuildDir)
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        //获取所有被该注解标记的类
        resolver.getSymbolsWithAnnotation(DISCOVERABLE).forEach {
            it.accept(visitor, Unit)
        }
        return emptyList()
    }

    override fun finish() {
        DiscoveryCore.saveDiscoverable(discoverableSet)
        env.logger.info("discovery processor finish. found discoverable: $discoverableSet")
    }

    private companion object {

        private const val DISCOVERABLE = "cn.numeron.discovery.Discoverable"

    }

}
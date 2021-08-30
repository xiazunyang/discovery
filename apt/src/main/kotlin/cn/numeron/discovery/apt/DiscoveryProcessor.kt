package cn.numeron.discovery.apt

import cn.numeron.discovery.core.DiscoveryConfig
import cn.numeron.discovery.core.DiscoveryCore
import cn.numeron.discovery.core.Modes
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

class DiscoveryProcessor : AbstractProcessor() {

    private lateinit var elementUtils: Elements
    private lateinit var discoveryConfig: DiscoveryConfig
    private lateinit var discoverableVisitor: DiscoverableVisitor
    private lateinit var implementationVisitor: ImplementationVisitor

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)

        elementUtils = processingEnv.elementUtils
        discoverableVisitor = DiscoverableVisitor(processingEnv)
        implementationVisitor = ImplementationVisitor(processingEnv)
        //初始化Discovery的工作目录
        val projectName = processingEnv.options[DiscoveryCore.PROJECT_NAME]
        val rootProjectBuildDir = processingEnv.options[DiscoveryCore.ROOT_PROJECT_BUILD_DIR]
        DiscoveryCore.init(projectName, rootProjectBuildDir)
        //加载Discovery的配置
        discoveryConfig = DiscoveryCore.loadConfig()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        val mutableSet = mutableSetOf(DISCOVERABLE)
        if (discoveryConfig.workMode == Modes.Mark) {
            //如果工作模式为标记的话，则添加Implementation注解
            mutableSet.add(IMPLEMENTATION)
        }
        return mutableSet
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement(DISCOVERABLE)).forEach {
            it.accept(discoverableVisitor, Unit)
        }
        DiscoveryCore.saveDiscoverable(discoverableVisitor.discoverableSet)
        if (discoveryConfig.workMode == Modes.Mark) {
            //如果工作模式为标记的话，则扫描Implementation
            roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement(IMPLEMENTATION)).forEach {
                it.accept(implementationVisitor, Unit)
            }
            DiscoveryCore.saveImplementation(implementationVisitor.implementationSet)
        }
        return true
    }

    private companion object {

        private const val DISCOVERABLE = "cn.numeron.discovery.Discoverable"
        private const val IMPLEMENTATION = "cn.numeron.discovery.Implementation"

    }

}
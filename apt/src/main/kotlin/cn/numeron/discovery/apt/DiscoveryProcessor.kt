package cn.numeron.discovery.apt

import cn.numeron.discovery.core.DiscoveryCore
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

class DiscoveryProcessor : AbstractProcessor() {

    private lateinit var elementUtils: Elements

    private val discoverableSet = mutableSetOf<String>()

    private lateinit var elementVisitor: DiscoverableVisitor

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)

        elementUtils = processingEnv.elementUtils
        elementVisitor = DiscoverableVisitor(discoverableSet, processingEnv)

        val projectName = processingEnv.options[DiscoveryCore.PROJECT_NAME]
        val rootProjectBuildDir = processingEnv.options[DiscoveryCore.ROOT_PROJECT_BUILD_DIR]
        DiscoveryCore.init(projectName, rootProjectBuildDir)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf("cn.numeron.discovery.Discoverable")
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        annotations.map<TypeElement, MutableSet<out Element>>(roundEnv::getElementsAnnotatedWith)
            .flatten()
            .forEach {
                it.accept(elementVisitor, Unit)
            }
        DiscoveryCore.saveDiscoverable(discoverableSet)
        return true
    }

}
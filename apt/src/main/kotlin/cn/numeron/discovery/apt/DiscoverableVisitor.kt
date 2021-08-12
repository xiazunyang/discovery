package cn.numeron.discovery.apt

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.util.SimpleElementVisitor9

class DiscoverableVisitor(private val processingEnv: ProcessingEnvironment) : SimpleElementVisitor9<Unit, Unit>(Unit) {

    private val discoverableMutableSet = mutableSetOf<String>()

    val discoverableSet: Set<String>
        get() = discoverableMutableSet

    override fun visitType(element: TypeElement, p: Unit) {
        if (element.kind == ElementKind.INTERFACE) {
            val discoverable = element.qualifiedName.toString()
            discoverableMutableSet.add(discoverable)
        }
    }

}
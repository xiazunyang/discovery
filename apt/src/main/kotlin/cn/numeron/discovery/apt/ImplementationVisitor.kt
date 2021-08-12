package cn.numeron.discovery.apt

import cn.numeron.discovery.core.DiscoverableImpl
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.SimpleElementVisitor9

class ImplementationVisitor(
    private val processingEnv: ProcessingEnvironment
) : SimpleElementVisitor9<Unit, Unit>(Unit) {

    private val implementationMutableSet = mutableSetOf<DiscoverableImpl>()

    val implementationSet: Set<DiscoverableImpl>
        get() = implementationMutableSet

    override fun visitType(element: TypeElement, p: Unit) {
        if (element.kind == ElementKind.CLASS) {
            val qualifierName = element.qualifiedName.toString()
            element.interfaces.map(TypeMirror::toString).forEach {
                implementationMutableSet.add(DiscoverableImpl(qualifierName, it))
            }
        }
    }

}
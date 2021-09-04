package cn.numeron.discovery.apt

import cn.numeron.discovery.core.DiscoverableImpl
import cn.numeron.discovery.core.DiscoveryCore
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.SimpleElementVisitor9
import javax.tools.Diagnostic

class ImplementationVisitor(
    private val env: ProcessingEnvironment
) : SimpleElementVisitor9<Unit, Unit>(Unit) {

    val implementationSet by lazy(DiscoveryCore::loadImplementation)

    override fun visitType(element: TypeElement, p: Unit) {
        if (element.kind == ElementKind.CLASS) {
            val hasNoArgsConstructor = element.enclosedElements
                .filterIsInstance<ExecutableElement>()
                .filter {
                    it.simpleName.contentEquals("<init>")
                }
                .any {
                    it.parameters.isEmpty()
                }
            if (hasNoArgsConstructor) {
                val qualifierName = element.qualifiedName.toString()
                element.interfaces.map(TypeMirror::toString).forEach {
                    implementationSet.add(DiscoverableImpl(qualifierName, it))
                }
            } else {
                //如果没有无参构造器，则抛出异常
                env.messager.printMessage(Diagnostic.Kind.ERROR, "The implementation class must have a no-argument constructor.", element)
                throw RuntimeException("The implementation class must have a no-argument constructor: $element")
            }
        }
    }

}
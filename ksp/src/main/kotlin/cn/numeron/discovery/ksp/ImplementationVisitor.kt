package cn.numeron.discovery.ksp

import cn.numeron.discovery.core.DiscoverableImpl
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*

class ImplementationVisitor(private val env: SymbolProcessorEnvironment) : KSVisitorVoid() {

    private val implementationMutableSet = mutableSetOf<DiscoverableImpl>()

    val implementationSet: Set<DiscoverableImpl>
        get() = implementationMutableSet

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        if (classDeclaration.classKind == ClassKind.CLASS) {
            val implementationName = classDeclaration.getQualifierName()
            classDeclaration.superTypes.map(KSTypeReference::resolve)
                .map(KSType::declaration)
                .filter(KSDeclaration::isInterface)
                .forEach {
                    val discoverableImpl = DiscoverableImpl(implementationName, it.getQualifierName())
                    implementationMutableSet.add(discoverableImpl)
                }
        }
    }

}
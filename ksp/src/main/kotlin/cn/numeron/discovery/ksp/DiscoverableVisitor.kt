package cn.numeron.discovery.ksp

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*

class DiscoverableVisitor(private val env: SymbolProcessorEnvironment) : KSVisitorVoid() {

    private val discoverableMutableSet = mutableSetOf<String>()

    val discoverableSet: Set<String>
        get() = discoverableMutableSet

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        if (classDeclaration.classKind == ClassKind.INTERFACE) {
            //当被注解标记的类是一个接口的时候，将该类添加到待处理列表中
            val discoverable = classDeclaration.getQualifierName()
            discoverableMutableSet.add(discoverable)
        }
    }

}
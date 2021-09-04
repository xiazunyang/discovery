package cn.numeron.discovery.ksp

import cn.numeron.discovery.core.DiscoverableImpl
import cn.numeron.discovery.core.DiscoveryCore
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*

class ImplementationVisitor(private val env: SymbolProcessorEnvironment) : KSVisitorVoid() {

    val implementationSet by lazy(DiscoveryCore::loadImplementation)

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        if (classDeclaration.classKind == ClassKind.CLASS) {
            //判断是否拥有无参构造方法
            val hasNoArgsConstructor = classDeclaration.getConstructors().any {
                it.parameters.isEmpty()
            }
            if (hasNoArgsConstructor) {
                //如果有，则添加到实现集合中
                classDeclaration.superTypes.map(KSTypeReference::resolve)
                    .map(KSType::declaration)
                    .filter(KSDeclaration::isInterface)
                    .forEach {
                        val implementationName = classDeclaration.getQualifierName()
                        val discoverableImpl = DiscoverableImpl(implementationName, it.getQualifierName())
                        implementationSet.add(discoverableImpl)
                    }
            } else {
                //如果没有无参构造器，则抛出异常
                env.logger.error("The implementation class must have a no-argument constructor.", classDeclaration)
                throw RuntimeException("The implementation class must have a no-argument constructor: $classDeclaration")
            }
        }
    }

}
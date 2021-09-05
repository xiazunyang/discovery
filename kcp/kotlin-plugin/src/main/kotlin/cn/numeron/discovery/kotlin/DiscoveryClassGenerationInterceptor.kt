package cn.numeron.discovery.kotlin

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin

class DiscoveryClassGenerationInterceptor : ClassBuilderInterceptorExtension {

    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink
    ): ClassBuilderFactory = object : ClassBuilderFactory by interceptedFactory {

        override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder {
            Logger.log("ClassBuilderFactory newClassBuilder descriptor = [${origin.descriptor}], parametersForJvmOverload = [${origin.parametersForJvmOverload}], originKind = [${origin.originKind}], element = [${origin.element}]")
            val newClassBuilder = interceptedFactory.newClassBuilder(origin)
            if (Name.identifier("class name") == origin.descriptor?.name) {
                return DiscoveryClassBuilder(newClassBuilder, emptyMap())
            }
            return newClassBuilder
        }

    }

}
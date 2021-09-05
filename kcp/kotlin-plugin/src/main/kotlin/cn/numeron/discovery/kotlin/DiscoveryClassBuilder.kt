package cn.numeron.discovery.kotlin

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

class DiscoveryClassBuilder(
    private val delegate: ClassBuilder,
    private val implementationSet: Map<String, List<String>>
) : DelegatingClassBuilder() {

    override fun getDelegate(): ClassBuilder = delegate

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        Logger.log("DiscoveryClassBuilder newMethod name = [$name], desc = [$desc], signature = [$signature], exceptions = [${exceptions?.contentToString()}]")
        val method = super.newMethod(origin, access, name, desc, signature, exceptions)
        if (name == "<init>" && desc == "()V") {
            return DiscoveryMethodVisitor(Opcodes.ASM8, method, access, name, desc, implementationSet)
        }
        return method
    }

}
package cn.numeron.discovery.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method

class DiscoveryClassVisitor(
    classVisitor: ClassVisitor,
    private val discoverableImpl: Map<String, List<String>>
) : ClassVisitor(Opcodes.ASM9, classVisitor) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "<init>" && descriptor == "()V") {
            return DiscoveryAdviceAdapter(api, methodVisitor, access, name, descriptor)
        }
        return methodVisitor
    }

    inner class DiscoveryAdviceAdapter(
        api: Int,
        methodVisitor: MethodVisitor?,
        access: Int,
        name: String?,
        descriptor: String?
    ) : AdviceAdapter(api, methodVisitor, access, name, descriptor) {

        private val discoveriesType = Type.getType("Lcn/numeron/discovery/Discoveries;")
        private val addImplementationMethod = Method("addImplementation", "(Ljava/lang/String;Ljava/lang/String;)V")

        override fun visitInsn(opcode: Int) {
            if (opcode == ARETURN || opcode == RETURN) {
                discoverableImpl.forEach { (discoverable, implList) ->
                    implList.forEach { implementation ->
                        insertCode(discoverable, implementation)
                    }
                }
            }
            super.visitInsn(opcode)
        }

        private fun insertCode(discoverable: String, implementation: String) {
            //ALOAD 0
            loadThis()
            //LDC "discoverable"
            visitLdcInsn(discoverable)
            //LDC "implementation"
            visitLdcInsn(implementation)
            //INVOKESPECIAL cn/numeron/discovery/Discoveries.addImplementation (Ljava/lang/String;Ljava/lang/String;)V
            invokeVirtual(discoveriesType, addImplementationMethod)
        }

    }

}
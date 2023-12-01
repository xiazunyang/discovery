package cn.numeron.discovery

import org.objectweb.asm.*
import org.objectweb.asm.commons.Method
import org.objectweb.asm.commons.AdviceAdapter

class DiscoveriesClassModifyVisitor(
    classVisitor: ClassVisitor,
    private val discoveries: Map<String, List<String>>
) : ClassVisitor(Opcodes.ASM7, classVisitor) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "<init>" && descriptor == "()V") {
            return DiscoveryConstructorAdviceAdapter(api, methodVisitor, access, name, descriptor)
        }
        return methodVisitor
    }

    inner class DiscoveryConstructorAdviceAdapter(
        api: Int,
        methodVisitor: MethodVisitor?,
        access: Int,
        name: String?,
        descriptor: String?
    ) : AdviceAdapter(api, methodVisitor, access, name, descriptor) {

        private var intercept = true

        private val discoveriesType = Type.getType("Lcn/numeron/discovery/Discoveries;")
        private val addImplementationMethod = Method("addImplementation", "(Ljava/lang/String;Ljava/lang/String;)V")

        override fun visitLdcInsn(value: Any?) {
            //在开始织入代码之前，拦截所有的局部变量声明
            if (!intercept) {
                super.visitLdcInsn(value)
            }
        }

        override fun visitMethodInsn(
            opcodeAndSource: Int,
            owner: String?,
            name: String?,
            descriptor: String?,
            isInterface: Boolean
        ) {
            //在开始织入代码之前，拦截所有的addImplementation调用
            if (!intercept || "addImplementation" != name) {
                super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface)
            }
        }

        override fun onMethodExit(opcode: Int) {
            intercept = false
            discoveries.forEach { (discoverable, implementations) ->
                implementations.forEach { implementation ->
                    insertCode(discoverable, implementation)
                }
            }
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
package cn.numeron.discovery.kcp

import org.jetbrains.org.objectweb.asm.*
import org.jetbrains.org.objectweb.asm.commons.*

class DiscoveryMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor?,
    access: Int,
    name: String?,
    descriptor: String?,
    private val discoverableImpl: Map<String, List<String>>
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
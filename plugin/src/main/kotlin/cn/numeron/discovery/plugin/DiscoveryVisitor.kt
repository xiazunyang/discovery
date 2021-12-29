package cn.numeron.discovery.plugin

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class DiscoveryVisitor : ClassNode(Opcodes.ASM7) {

    val order: Int
        get() = invisibleAnnotations.first {
            it.desc == IMPLEMENTATION_ANNOTATION
        }.values?.let {
            it[it.indexOf("order") + 1].toString().toInt()
        } ?: 0

    fun hasAnnotation(annotation: String): Boolean {
        invisibleAnnotations ?: return false
        return invisibleAnnotations.any {
            it.desc == annotation
        }
    }

    fun isAbstract(): Boolean {
        return access and Opcodes.ACC_ABSTRACT != 0
    }

}
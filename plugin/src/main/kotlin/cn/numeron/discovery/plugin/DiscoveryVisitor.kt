package cn.numeron.discovery.plugin

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class DiscoveryVisitor : ClassNode(Opcodes.ASM7) {

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
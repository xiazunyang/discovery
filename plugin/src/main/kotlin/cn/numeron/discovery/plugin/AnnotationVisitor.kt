package cn.numeron.discovery.plugin

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class AnnotationVisitor : ClassNode(Opcodes.ASM9) {

    fun hasAnnotation(annotation: String): Boolean {
        invisibleAnnotations ?: return false
        return invisibleAnnotations.any {
            it.desc == annotation
        }
    }

    fun hasNoArgConstructor(): Boolean {
        if (methods.isNullOrEmpty()) {
            return false
        }
        return methods.any {
            "<init>" == it.name && "()V" == it.desc && it.parameters.isNullOrEmpty()
        }
    }

    fun isInterface(): Boolean {
        return access and Opcodes.ACC_INTERFACE != 0
    }

}
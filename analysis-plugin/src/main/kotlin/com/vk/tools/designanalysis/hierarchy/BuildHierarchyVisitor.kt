package com.vk.tools.designanalysis.hierarchy

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class BuildHierarchyVisitor(
    private val simpleClassHierarchy: SimpleClassHierarchy
) : ClassVisitor(Opcodes.ASM9) {
    private lateinit var className: String

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name
        if (superName != null) {
            simpleClassHierarchy.setSuperclassOf(name, superName)
        }
        if (interfaces != null) {
            for (interfaceName in interfaces) {
                simpleClassHierarchy.setImplementedOf(name, interfaceName)
            }
        }
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String?>?
    ): MethodVisitor {
        if (access and Opcodes.ACC_BRIDGE == 0) {
            simpleClassHierarchy.addDeclaredMethod(DeclaredMethod.make(className, name, descriptor))
        }
        return BuildHierarchyMethodVisitor(simpleClassHierarchy)
    }

    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        value: Any?
    ): FieldVisitor? {
        simpleClassHierarchy.addDeclaredField(DeclaredField(className, name, descriptor))
        return null
    }

    private class BuildHierarchyMethodVisitor(
        private val simpleClassHierarchy: SimpleClassHierarchy
    ) : MethodVisitor(Opcodes.ASM9) {
        override fun visitMethodInsn(
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean
        ) {
            simpleClassHierarchy.addDeclaredMethod(DeclaredMethod.make(owner, name, descriptor))
        }
    }
}

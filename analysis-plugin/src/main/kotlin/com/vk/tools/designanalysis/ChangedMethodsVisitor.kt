package com.vk.tools.designanalysis

import com.vk.tools.designanalysis.hierarchy.DeclaredField
import com.vk.tools.designanalysis.hierarchy.DeclaredMethod
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.Field
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.MethodArgument
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.MethodReturn
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class ChangedMethodsVisitor(
    private val allTainted: Set<PossiblyTaintedSource>,
    private val changedMethods: MutableSet<DeclaredMethod>,
) : ClassVisitor(Opcodes.ASM9, null) {
    private var className: String = ""

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String?>?
    ) {
        className = name
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String?>?
    ): MethodVisitor? {
        val thisMethod = DeclaredMethod.make(className, name, descriptor)
        if (MethodReturn(thisMethod) in allTainted) {
            changedMethods.add(thisMethod)
            return null
        }

        val thisRefSize = if (access and Opcodes.ACC_STATIC == 0) 1 else 0
        for (i in 0 until Type.getArgumentCount(descriptor)) {
            if (MethodArgument(thisMethod, i + thisRefSize) in allTainted) {
                changedMethods.add(thisMethod)
                return null
            }
        }

        return ChangedMethodVisitor(thisMethod, allTainted, changedMethods)
    }
}

private class ChangedMethodVisitor(
    private val thisMethod: DeclaredMethod,
    private val allTainted: Set<PossiblyTaintedSource>,
    private val changedMethods: MutableSet<DeclaredMethod>,
) : MethodVisitor(Opcodes.ASM9) {
    private var isTainted = false

    override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        if (isTainted) {
            return
        }

        val fieldRef = DeclaredField(owner, name, descriptor)
        isTainted = Field(fieldRef) in allTainted
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        if (isTainted) {
            return
        }

        val methodRef = DeclaredMethod.make(owner, name, descriptor)
        if (MethodReturn(methodRef) in allTainted) {
            isTainted = true
            return
        }

        for (i in 0 until Type.getArgumentCount(descriptor)) {
            if (MethodArgument(methodRef, i) in allTainted) {
                isTainted = true
                return
            }
        }
    }

    override fun visitEnd() {
        if (isTainted) {
            changedMethods.add(thisMethod)
        }
    }
}

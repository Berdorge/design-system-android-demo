package com.vk.tools.designanalysis.instrumentation

import com.vk.tools.designanalysis.RUNTIME_STATE_DESC
import com.vk.tools.designanalysis.getGeneratedFieldName
import com.vk.tools.designanalysis.isAttrClass
import com.vk.tools.designanalysis.isDimenClass
import com.vk.tools.designanalysis.newRuntimeState
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

class CreateApplicationFieldMarksVisitor(
    private val delegate: ClassVisitor,
) : ClassVisitor(Opcodes.ASM9, ClassNode(Opcodes.ASM9)) {
    private val classNode = cv as ClassNode

    override fun visitEnd() {
        super.visitEnd()

        val mark = when {
            classNode.isAttrClass() -> "attribute"
            classNode.isDimenClass() -> "resource"
            else -> null
        }
        if (mark != null) {
            initializeRSubclassStateFields(mark)
        }
        createRuntimeStateForApplicationFields()

        classNode.accept(delegate)
    }

    private fun initializeRSubclassStateFields(mark: String) {
        val method = classNode.methods.find { node ->
            node.name == "<clinit>"
        } ?: MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null).also {
            classNode.methods.add(it)
            it.visitInsn(Opcodes.RETURN)
        }
        for (field in classNode.fields.toList()) {
            if (isAllowedType(Type.getType(field.desc))) {
                method.instructions.insert(
                    FieldInsnNode(
                        Opcodes.PUTSTATIC,
                        classNode.name,
                        getGeneratedFieldName(field.name),
                        RUNTIME_STATE_DESC
                    )
                )
                method.instructions.insert(newRuntimeState("$mark ${field.name}"))
            }
        }
    }

    private fun createRuntimeStateForApplicationFields() {
        for (field in classNode.fields.toList()) {
            if (isAllowedType(Type.getType(field.desc))) {
                val publicAccess = field.access.and(Opcodes.ACC_PROTECTED.inv())
                    .and(Opcodes.ACC_PRIVATE.inv())
                    .or(Opcodes.ACC_PUBLIC)
                classNode.fields.add(
                    FieldNode(
                        publicAccess,
                        getGeneratedFieldName(field.name),
                        RUNTIME_STATE_DESC,
                        null,
                        null
                    )
                )
            }
        }
    }
}

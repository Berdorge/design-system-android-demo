package com.vk.tools.designanalysis

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode

internal const val RUNTIME_HELPER_NAME = "com/vkui/analysis/runtime/DesignAnalysisHelper"
internal const val RUNTIME_HELPER_DESC = "L$RUNTIME_HELPER_NAME;"
internal const val RUNTIME_STATE_NAME = "com/vkui/analysis/runtime/DesignAnalysisMark"
internal const val RUNTIME_STATE_DESC = "L$RUNTIME_STATE_NAME;"
internal const val RUNTIME_TRACKER_NAME = "com/vkui/analysis/runtime/DesignAnalysisTracker"

internal fun Type.isPrimitive(): Boolean = when (this) {
    Type.BOOLEAN_TYPE,
    Type.CHAR_TYPE,
    Type.BYTE_TYPE,
    Type.SHORT_TYPE,
    Type.INT_TYPE,
    Type.FLOAT_TYPE,
    Type.LONG_TYPE,
    Type.DOUBLE_TYPE -> true

    else -> false
}

internal fun ClassNode.isAttrClass() = name.isAttrClass()

internal fun ClassNode.isDimenClass() = name.isDimenClass()

internal fun ClassNode.isNotRSubclass() = !isAttrClass() && !isDimenClass()

internal fun String.isAttrClass() = endsWith("/R\$attr")

internal fun String.isDimenClass() = endsWith("/R\$dimen")

internal fun String.isNotRSubclass() = !isAttrClass() && !isDimenClass()

internal fun instructionListOf(vararg instructions: AbstractInsnNode) = InsnList().apply {
    for (instruction in instructions) {
        add(instruction)
    }
}

internal fun getGeneratedFieldName(name: String?) = "$name\$designAnalysis"

internal fun newRuntimeState(value: String) = instructionListOf(
    TypeInsnNode(Opcodes.NEW, RUNTIME_STATE_NAME),
    InsnNode(Opcodes.DUP),
    LdcInsnNode(value),
    MethodInsnNode(
        Opcodes.INVOKESPECIAL,
        RUNTIME_STATE_NAME,
        "<init>",
        "(Ljava/lang/String;)V"
    )
)

package com.vk.tools.designanalysis.instrumentation

import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.analysis.Value

data class AnalysisAbstractValue(val type: Type?, val origins: Set<ValueOrigin>) : Value {
    constructor(type: Type?, origin: AbstractInsnNode) : this(type, setOf(ValueOrigin.Insn(origin)))

    constructor(type: Type?, origin: ValueOrigin) : this(type, setOf(origin))

    override fun getSize(): Int = type?.size ?: 1
}

sealed class ValueOrigin {
    data class Insn(val insn: AbstractInsnNode) : ValueOrigin()
    data class Parameter(val local: Int) : ValueOrigin()
}

fun isAllowedType(type: Type) =
    type == Type.INT_TYPE
        || type == Type.FLOAT_TYPE
        || type == Type.DOUBLE_TYPE

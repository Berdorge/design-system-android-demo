package com.vk.tools.designanalysis.instrumentation

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.analysis.Interpreter

class AnalysisSourceInterpreter(api: Int) : Interpreter<AnalysisAbstractValue>(api) {
    override fun newValue(type: Type?): AnalysisAbstractValue? {
        if (type === Type.VOID_TYPE) {
            return null
        }
        return AnalysisAbstractValue(type, emptySet())
    }

    override fun newParameterValue(
        isInstanceMethod: Boolean,
        local: Int,
        type: Type
    ): AnalysisAbstractValue? {
        return if (isAllowedType(type)) {
            AnalysisAbstractValue(type, ValueOrigin.Parameter(local))
        } else {
            AnalysisAbstractValue(type, emptySet())
        }
    }

    override fun newOperation(insn: AbstractInsnNode): AnalysisAbstractValue {
        val type = when (insn.opcode) {
            Opcodes.LCONST_0,
            Opcodes.LCONST_1 -> Type.LONG_TYPE

            Opcodes.DCONST_0,
            Opcodes.DCONST_1 -> Type.DOUBLE_TYPE

            Opcodes.ICONST_M1,
            Opcodes.ICONST_0,
            Opcodes.ICONST_1,
            Opcodes.ICONST_2,
            Opcodes.ICONST_3,
            Opcodes.ICONST_4,
            Opcodes.ICONST_5,
            Opcodes.BIPUSH,
            Opcodes.SIPUSH -> Type.INT_TYPE

            Opcodes.FCONST_0,
            Opcodes.FCONST_1,
            Opcodes.FCONST_2 -> Type.FLOAT_TYPE

            Opcodes.LDC -> {
                when (val value = (insn as LdcInsnNode).cst) {
                    is Int -> Type.INT_TYPE
                    is Float -> Type.FLOAT_TYPE
                    is Long -> Type.LONG_TYPE
                    is Double -> Type.DOUBLE_TYPE
                    else -> Type.getType(value.javaClass)
                }
            }

            Opcodes.GETSTATIC -> Type.getType((insn as FieldInsnNode).desc)

            else -> Type.getType("Ljava/lang/Object;")
        }
        return if (isAllowedType(type)) {
            AnalysisAbstractValue(type, insn)
        } else {
            AnalysisAbstractValue(type, emptySet())
        }
    }

    override fun copyOperation(
        insn: AbstractInsnNode,
        value: AnalysisAbstractValue
    ): AnalysisAbstractValue {
        when (insn.opcode) {
            Opcodes.ILOAD,
            Opcodes.ISTORE,
            Opcodes.FLOAD,
            Opcodes.FSTORE,
            Opcodes.DLOAD,
            Opcodes.DSTORE,
            Opcodes.DUP,
            Opcodes.DUP2,
            Opcodes.DUP_X1,
            Opcodes.DUP_X2,
            Opcodes.DUP2_X1,
            Opcodes.DUP2_X2,
            Opcodes.SWAP -> {
                return value
            }
        }
        return AnalysisAbstractValue(value.type, emptySet())
    }

    override fun unaryOperation(
        insn: AbstractInsnNode,
        value: AnalysisAbstractValue
    ): AnalysisAbstractValue {
        return when (insn.opcode) {
            Opcodes.IRETURN,
            Opcodes.FRETURN,
            Opcodes.DRETURN -> value

            Opcodes.I2D,
            Opcodes.F2D,
            Opcodes.L2D -> AnalysisAbstractValue(Type.DOUBLE_TYPE, value.origins)

            Opcodes.F2I,
            Opcodes.L2I,
            Opcodes.D2I -> AnalysisAbstractValue(Type.INT_TYPE, value.origins)

            Opcodes.I2F,
            Opcodes.L2F,
            Opcodes.D2F -> AnalysisAbstractValue(Type.FLOAT_TYPE, value.origins)

            Opcodes.I2L,
            Opcodes.F2L,
            Opcodes.D2L -> AnalysisAbstractValue(Type.LONG_TYPE, emptySet())

            Opcodes.INEG,
            Opcodes.FNEG,
            Opcodes.LNEG,
            Opcodes.DNEG -> AnalysisAbstractValue(value.type, insn)

            Opcodes.GETFIELD -> {
                val type = Type.getType((insn as FieldInsnNode).desc)
                if (isAllowedType(type)) {
                    AnalysisAbstractValue(type, insn)
                } else {
                    AnalysisAbstractValue(type, emptySet())
                }
            }

            else -> AnalysisAbstractValue(Type.getType("Ljava/lang/Object;"), emptySet())
        }
    }

    override fun binaryOperation(
        insn: AbstractInsnNode,
        value1: AnalysisAbstractValue,
        value2: AnalysisAbstractValue
    ): AnalysisAbstractValue {
        val type = when (insn.opcode) {
            Opcodes.IALOAD,
            Opcodes.IADD,
            Opcodes.ISUB,
            Opcodes.IMUL,
            Opcodes.IDIV,
            Opcodes.IREM,
            Opcodes.ISHL,
            Opcodes.ISHR,
            Opcodes.IUSHR,
            Opcodes.IAND,
            Opcodes.IOR,
            Opcodes.IXOR -> Type.INT_TYPE

            Opcodes.FALOAD,
            Opcodes.FADD,
            Opcodes.FSUB,
            Opcodes.FMUL,
            Opcodes.FDIV,
            Opcodes.FREM -> Type.FLOAT_TYPE

            Opcodes.LALOAD,
            Opcodes.LADD,
            Opcodes.LSUB,
            Opcodes.LMUL,
            Opcodes.LDIV,
            Opcodes.LREM,
            Opcodes.LSHL,
            Opcodes.LSHR,
            Opcodes.LUSHR,
            Opcodes.LAND,
            Opcodes.LOR,
            Opcodes.LXOR -> Type.LONG_TYPE

            Opcodes.DALOAD,
            Opcodes.DADD,
            Opcodes.DSUB,
            Opcodes.DMUL,
            Opcodes.DDIV,
            Opcodes.DREM -> Type.DOUBLE_TYPE

            else -> Type.getType("Ljava/lang/Object;")
        }
        return AnalysisAbstractValue(type, insn)
    }

    override fun ternaryOperation(
        insn: AbstractInsnNode,
        value1: AnalysisAbstractValue,
        value2: AnalysisAbstractValue,
        value3: AnalysisAbstractValue
    ): AnalysisAbstractValue {
        return AnalysisAbstractValue(Type.VOID_TYPE, emptySet())
    }

    override fun naryOperation(
        insn: AbstractInsnNode,
        values: MutableList<out AnalysisAbstractValue>
    ): AnalysisAbstractValue {
        return when (insn.opcode) {
            Opcodes.MULTIANEWARRAY -> AnalysisAbstractValue(
                Type.getType("Ljava/lang/Object;"),
                emptySet()
            )

            Opcodes.INVOKEDYNAMIC -> {
                val returnType = Type.getReturnType((insn as InvokeDynamicInsnNode).desc)
                if (isAllowedType(returnType)) {
                    AnalysisAbstractValue(returnType, insn)
                } else {
                    AnalysisAbstractValue(returnType, emptySet())
                }
            }

            else -> {
                val returnType = Type.getReturnType((insn as MethodInsnNode).desc)
                if (isAllowedType(returnType)) {
                    AnalysisAbstractValue(returnType, insn)
                } else {
                    AnalysisAbstractValue(returnType, emptySet())
                }
            }
        }
    }

    override fun returnOperation(
        insn: AbstractInsnNode,
        value: AnalysisAbstractValue,
        expected: AnalysisAbstractValue
    ) {
        // Nothing to do.
    }

    override fun merge(
        value1: AnalysisAbstractValue,
        value2: AnalysisAbstractValue
    ): AnalysisAbstractValue {
        if (value1.getSize() != value2.getSize() || !value1.origins.containsAll(value2.origins)) {
            val newType = if (value1.type == value2.type) {
                value1.type
            } else {
                Type.getType("Ljava/lang/Object;")
            }
            return AnalysisAbstractValue(newType, value1.origins + value2.origins)
        }
        return value1
    }
}

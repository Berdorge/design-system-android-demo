package com.vk.tools.designanalysis.taint

import com.vk.tools.designanalysis.hierarchy.DeclaredField
import com.vk.tools.designanalysis.hierarchy.DeclaredMethod
import com.vk.tools.designanalysis.instrumentation.isAllowedType
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.Field
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.MethodArgument
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.MethodReturn
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.analysis.Interpreter
import kotlin.math.min

class TaintInterpreter(
    api: Int,
    private val thisMethod: DeclaredMethod,
) : Interpreter<PossiblyTaintedValue>(api) {
    override fun newValue(type: Type?): PossiblyTaintedValue? {
        if (type == Type.VOID_TYPE) {
            return null
        }
        return PossiblyTaintedValue(type?.size ?: 1, emptySet())
    }

    override fun newParameterValue(
        isInstanceMethod: Boolean,
        local: Int,
        type: Type
    ): PossiblyTaintedValue {
        val argumentIndex = if (isInstanceMethod) local - 1 else local
        return when (type) {
            Type.INT_TYPE,
            Type.FLOAT_TYPE -> PossiblyTaintedValue(
                valueSize = 1,
                sources = setOf(MethodArgument(thisMethod, argumentIndex))
            )

            Type.DOUBLE_TYPE -> PossiblyTaintedValue(
                valueSize = 2,
                sources = setOf(MethodArgument(thisMethod, argumentIndex))
            )

            else -> PossiblyTaintedValue(type.size, emptySet())
        }
    }

    override fun newOperation(insn: AbstractInsnNode): PossiblyTaintedValue {
        return when (insn.opcode) {
            Opcodes.LCONST_0,
            Opcodes.LCONST_1,
            Opcodes.DCONST_0,
            Opcodes.DCONST_1 -> PossiblyTaintedValue(2, emptySet())

            Opcodes.LDC -> {
                val value = (insn as LdcInsnNode).cst
                val size = if (value is Long || value is Double) {
                    2
                } else {
                    1
                }
                PossiblyTaintedValue(size, emptySet())
            }

            Opcodes.GETSTATIC -> {
                insn as FieldInsnNode
                val fieldType = Type.getType(insn.desc)
                if (isAllowedType(fieldType)) {
                    val fieldReference = DeclaredField(
                        insn.owner,
                        insn.name,
                        insn.desc
                    )
                    PossiblyTaintedValue(fieldType.size, setOf(Field(fieldReference)))
                } else {
                    PossiblyTaintedValue(fieldType.size, emptySet())
                }
            }

            else -> PossiblyTaintedValue(1, emptySet())
        }
    }

    override fun copyOperation(
        insn: AbstractInsnNode,
        value: PossiblyTaintedValue
    ): PossiblyTaintedValue {
        return value
    }

    override fun unaryOperation(
        insn: AbstractInsnNode,
        value: PossiblyTaintedValue
    ): PossiblyTaintedValue {
        return when (insn.opcode) {
            Opcodes.INEG,
            Opcodes.FNEG,
            Opcodes.LNEG,
            Opcodes.DNEG,
            Opcodes.IINC,
            Opcodes.I2F,
            Opcodes.F2I,
            Opcodes.IRETURN,
            Opcodes.FRETURN,
            Opcodes.DRETURN -> value

            Opcodes.I2D,
            Opcodes.F2D -> PossiblyTaintedValue(2, value.sources)

            Opcodes.D2F,
            Opcodes.D2I -> PossiblyTaintedValue(1, value.sources)

            Opcodes.I2L,
            Opcodes.L2D,
            Opcodes.F2L,
            Opcodes.D2L -> PossiblyTaintedValue(2, emptySet())

            Opcodes.GETFIELD -> {
                insn as FieldInsnNode
                val fieldType = Type.getType(insn.desc)
                if (isAllowedType(fieldType)) {
                    val fieldReference = DeclaredField(
                        insn.owner,
                        insn.name,
                        insn.desc
                    )
                    PossiblyTaintedValue(fieldType.size, setOf(Field(fieldReference)))
                } else {
                    PossiblyTaintedValue(fieldType.size, emptySet())
                }
            }

            else -> PossiblyTaintedValue(1, emptySet())
        }
    }

    override fun binaryOperation(
        insn: AbstractInsnNode,
        lhs: PossiblyTaintedValue,
        rhs: PossiblyTaintedValue
    ): PossiblyTaintedValue {
        val size = when (insn.opcode) {
            Opcodes.LALOAD,
            Opcodes.DALOAD,
            Opcodes.LADD,
            Opcodes.DADD,
            Opcodes.LSUB,
            Opcodes.DSUB,
            Opcodes.LMUL,
            Opcodes.DMUL,
            Opcodes.LDIV,
            Opcodes.DDIV,
            Opcodes.LREM,
            Opcodes.DREM,
            Opcodes.LSHL,
            Opcodes.LSHR,
            Opcodes.LUSHR,
            Opcodes.LAND,
            Opcodes.LOR,
            Opcodes.LXOR -> 2

            else -> 1
        }
        return PossiblyTaintedValue(size, emptySet())
    }

    override fun ternaryOperation(
        insn: AbstractInsnNode,
        value1: PossiblyTaintedValue,
        value2: PossiblyTaintedValue,
        value3: PossiblyTaintedValue
    ): PossiblyTaintedValue {
        return PossiblyTaintedValue(1, emptySet())
    }

    override fun naryOperation(
        insn: AbstractInsnNode,
        args: List<PossiblyTaintedValue>
    ): PossiblyTaintedValue {
        if (insn !is MethodInsnNode) {
            return PossiblyTaintedValue(1, emptySet())
        }
        val returnType = Type.getReturnType(insn.desc)
        if (!isAllowedType(returnType)) {
            return PossiblyTaintedValue(returnType.size, emptySet())
        }
        val methodReference = DeclaredMethod.Companion.make(
            insn.owner,
            insn.name,
            insn.desc
        )
        return PossiblyTaintedValue(
            valueSize = returnType.size,
            sources = setOf(MethodReturn(methodReference))
        )
    }

    override fun returnOperation(
        insn: AbstractInsnNode,
        value: PossiblyTaintedValue,
        expected: PossiblyTaintedValue
    ) {
        // Nothing to do.
    }

    override fun merge(
        value1: PossiblyTaintedValue,
        value2: PossiblyTaintedValue
    ): PossiblyTaintedValue {
        return if (value1 == value2) {
            value1
        } else {
            PossiblyTaintedValue(
                valueSize = min(value1.size, value2.size),
                sources = value1.sources + value2.sources
            )
        }
    }
}

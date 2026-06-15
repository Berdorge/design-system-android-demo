package com.vk.tools.designanalysis.taint

import com.vk.tools.designanalysis.hierarchy.ClassHierarchy
import com.vk.tools.designanalysis.hierarchy.DeclaredField
import com.vk.tools.designanalysis.hierarchy.DeclaredMethod
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.Field
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.MethodReturn
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.analysis.Analyzer

class TaintVisitor(
//    private val delegate: ClassVisitor?,
    private val tainted: MutableMap<PossiblyTaintedSource, MutableSet<PossiblyTaintedSource>>,
    private val classHierarchy: ClassHierarchy,
) : ClassVisitor(Opcodes.ASM9, ClassNode(Opcodes.ASM9)) {
    override fun visitEnd() {
        super.visitEnd()

        val classNode = cv as ClassNode

        for (method in classNode.methods) {
            val thisMethod = DeclaredMethod.Companion.make(
                classNode.name,
                method.name,
                method.desc
            )
            val analyzer = Analyzer(TaintInterpreter(Opcodes.ASM9, thisMethod))
            analyzer.analyze(classNode.name, method)
            val frames = analyzer.frames

            for (i in frames.indices) {
                val frame = frames[i] ?: continue
                val insn = method.instructions[i]
                when {
                    insn.opcode == Opcodes.PUTFIELD || insn.opcode == Opcodes.PUTSTATIC -> {
                        insn as FieldInsnNode
                        val fieldReference = DeclaredField(insn.owner, insn.name, insn.desc)
                        val value = frame.getStack(frame.stackSize - 1)
                        for (source in value.sources) {
                            tainted.computeIfAbsent(Field(fieldReference)) {
                                mutableSetOf()
                            }.add(source)
                        }
                    }

                    insn.opcode in Opcodes.IRETURN..Opcodes.ARETURN -> {
                        val value = frame.getStack(frame.stackSize - 1)
                        for (source in value.sources) {
                            tainted.computeIfAbsent(MethodReturn(thisMethod)) {
                                mutableSetOf()
                            }.add(source)
                        }
                    }

                    insn is MethodInsnNode -> {
                        val calleeRef = DeclaredMethod.make(insn.owner, insn.name, insn.desc)
                        val possibleTargets = when (insn.opcode) {
                            INVOKESTATIC,
                            INVOKESPECIAL -> listOf(calleeRef)

                            else -> classHierarchy.getPossibleTargets(calleeRef)
                        }
                        val argumentCount = Type.getArgumentCount(insn.desc)
                        for (j in 0 until argumentCount) {
                            val value = frame.getStack(frame.stackSize - argumentCount + j)
                            for (source in value.sources) {
                                for (target in possibleTargets) {
                                    tainted.computeIfAbsent(
                                        PossiblyTaintedSource.MethodArgument(target, j)
                                    ) {
                                        mutableSetOf()
                                    }.add(source)
                                }
                            }
                        }
                    }
                }
            }
        }

//        classNode.accept(delegate)
    }
}

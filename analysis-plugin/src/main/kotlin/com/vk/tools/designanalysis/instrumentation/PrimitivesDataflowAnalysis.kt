package com.vk.tools.designanalysis.instrumentation

import com.vk.tools.designanalysis.RUNTIME_HELPER_NAME
import com.vk.tools.designanalysis.RUNTIME_STATE_DESC
import com.vk.tools.designanalysis.RUNTIME_STATE_NAME
import com.vk.tools.designanalysis.RUNTIME_TRACKER_NAME
import com.vk.tools.designanalysis.getGeneratedFieldName
import com.vk.tools.designanalysis.hierarchy.DeclaredField
import com.vk.tools.designanalysis.hierarchy.DeclaredMethod
import com.vk.tools.designanalysis.instructionListOf
import com.vk.tools.designanalysis.isNotRSubclass
import com.vk.tools.designanalysis.newRuntimeState
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.Field
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.MethodArgument
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.MethodReturn
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FrameNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.Frame
import kotlin.math.max

class PrimitivesDataflowAnalysis(
    delegate: ClassVisitor,
    private val validator: AnalysisValidator,
    private val isTainted: (PossiblyTaintedSource) -> Boolean,
    private val shouldInstrumentMethod: (DeclaredMethod) -> Boolean,
) : ClassVisitor(Opcodes.ASM9, delegate) {
    private var name: String = ""

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String?>?
    ) {
        this.name = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String?>?
    ): MethodVisitor? {
        val delegate = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (
            this.name != "kotlin/jvm/internal/Intrinsics" &&
            !this.name.startsWith("com/vkui/analysis/runtime") &&
            this.name.isNotRSubclass()
        ) {
            val methodNode = MethodNode(access, name, descriptor, signature, exceptions)
            return PrimitivesAnalysisMethodVisitor(methodNode, delegate)
        } else {
            return delegate
        }
    }

    private fun instrumentMethod(methodNode: MethodNode) {
        val argumentTypes = Type.getArgumentTypes(methodNode.desc)
        val isStatic = methodNode.access and Opcodes.ACC_STATIC != 0
        val thisReferenceSize = if (isStatic) 0 else 1
        val initialLocalsUnsized = thisReferenceSize + argumentTypes.size
        val initialLocalsSized = thisReferenceSize + argumentTypes.sumOf { it.size }
        val frames = Analyzer(AnalysisSourceInterpreter(api)).run {
            analyze(name, methodNode)
            frames
        }
        val usageInfo = calculateUsageInfo(methodNode, frames)
        val context = InstrumentationContext(
            method = methodNode,
            originalInsns = methodNode.instructions.toList(),
            initialLocalsUnsized = initialLocalsUnsized,
            initialLocalsSized = initialLocalsSized,
            argumentUses = usageInfo.argumentUses,
            instructionUses = usageInfo.instructionUses,
            dyingValuesAtFrame = usageInfo.dyingValuesAtFrame,
        )

        setInitialValueForArgumentSlots(context)

        for (i in frames.indices) {
            val frame = frames[i] ?: continue
            val instruction = context.originalInsns[i]

            readSlotsConsumedByInsn(context, instruction, frame)
            context.releaseDyingAtFrame(i)
            writeSlotsProducedByInsn(context, instruction)
        }

        allocateSlotsInLocals(context)
        setInitialValueForInstructionSlots(context)
    }

    private fun setInitialValueForArgumentSlots(context: InstrumentationContext) {
        repeat(context.initialLocalsSized) { i ->
            for (use in context.argumentUses[i].orEmpty()) {
                val slot = context.allocate(use)
                context.method.instructions.insert(
                    instructionListOf(
                        IntInsnNode(Opcodes.BIPUSH, i),
                        trackerCall("getFromParameter", "(I)${RUNTIME_STATE_DESC}"),
                        context.storeStateSlot(slot)
                    )
                )
            }
        }
    }

    private fun readSlotsConsumedByInsn(
        context: InstrumentationContext,
        insn: AbstractInsnNode,
        frame: Frame<AnalysisAbstractValue>,
    ) {
        when (insn.opcode) {
            in Opcodes.INVOKEVIRTUAL..Opcodes.INVOKEINTERFACE -> passArgumentsToCallee(
                context, insn as MethodInsnNode, frame
            )

            Opcodes.PUTFIELD -> rememberInstanceFieldMark(context, insn as FieldInsnNode, frame)

            Opcodes.PUTSTATIC -> rememberStaticFieldMark(context, insn as FieldInsnNode, frame)

            in Opcodes.IRETURN..Opcodes.DRETURN -> passReturnToCaller(context, insn, frame)

            in Opcodes.INEG..Opcodes.DNEG -> {
                val value = frame.getStack(frame.stackSize - 1)
                val uses = context.instructionUses[insn]
                val index = context.slotIndexOf(value)
                if (index >= 0 && uses != null) {
                    context.method.instructions.insertBefore(
                        insn,
                        instructionListOf(
                            context.loadStateSlot(index),
                            trackerCall("negate", "(${RUNTIME_STATE_DESC})${RUNTIME_STATE_DESC}"),
                        )
                    )
                    context.slots[index] = null
                    uses.forEachIndexed { i, use ->
                        val slot = context.allocate(use)
                        if (i != uses.size - 1) {
                            context.method.instructions.insertBefore(insn, InsnNode(Opcodes.DUP))
                        }
                        context.method.instructions.insertBefore(
                            insn,
                            context.storeStateSlot(slot)
                        )
                    }
                }
            }

            in Opcodes.IADD..Opcodes.LXOR -> {
                val leftValue = frame.getStack(frame.stackSize - 2)
                val rightValue = frame.getStack(frame.stackSize - 1)
                val leftIndex = context.slotIndexOf(leftValue)
                val rightIndex = context.slotIndexOf(rightValue)
                val uses = context.instructionUses[insn]
                if (leftIndex >= 0 && rightIndex >= 0 && uses != null) {
                    val description = when (insn.opcode) {
                        Opcodes.IADD, Opcodes.LADD, Opcodes.FADD, Opcodes.DADD -> "add"
                        Opcodes.ISUB, Opcodes.LSUB, Opcodes.FSUB, Opcodes.DSUB -> "sub"
                        Opcodes.IMUL, Opcodes.LMUL, Opcodes.FMUL, Opcodes.DMUL -> "mul"
                        Opcodes.IDIV, Opcodes.LDIV, Opcodes.FDIV, Opcodes.DDIV -> "div"
                        Opcodes.IREM, Opcodes.LREM, Opcodes.FREM, Opcodes.DREM -> "rem"
                        Opcodes.ISHL, Opcodes.LSHL -> "shl"
                        Opcodes.ISHR, Opcodes.LSHR -> "shr"
                        Opcodes.IUSHR, Opcodes.LUSHR -> "ushr"
                        Opcodes.IAND, Opcodes.LAND -> "and"
                        Opcodes.IOR, Opcodes.LOR -> "or"
                        Opcodes.IXOR, Opcodes.LXOR -> "xor"
                        else -> insn.opcode.toString()
                    }
                    context.method.instructions.insertBefore(
                        insn,
                        instructionListOf(
                            LdcInsnNode(description),
                            context.loadStateSlot(leftIndex),
                            context.loadStateSlot(rightIndex),
                            trackerCall(
                                "binaryOperation",
                                "(Ljava/lang/String;${RUNTIME_STATE_DESC}${RUNTIME_STATE_DESC})${RUNTIME_STATE_DESC}"
                            ),
                        )
                    )
                    context.slots[rightIndex] = null
                    uses.forEachIndexed { i, use ->
                        val slot = context.allocate(use)
                        if (i != uses.size - 1) {
                            context.method.instructions.insertBefore(insn, InsnNode(Opcodes.DUP))
                        }
                        context.method.instructions.insertBefore(
                            insn,
                            context.storeStateSlot(slot)
                        )
                    }
                }
            }
        }
    }

    private fun writeSlotsProducedByInsn(
        context: InstrumentationContext,
        insn: AbstractInsnNode,
    ) {
        val uses = context.instructionUses[insn].orEmpty()
        for (use in uses) {
            when (insn.opcode) {
                Opcodes.INVOKESTATIC,
                Opcodes.INVOKESPECIAL,
                Opcodes.INVOKEVIRTUAL,
                Opcodes.INVOKEINTERFACE -> getReturnFromCallee(
                    context, insn as MethodInsnNode, use
                )

                Opcodes.GETFIELD -> getInstanceFieldMark(context, insn as FieldInsnNode, use)

                Opcodes.GETSTATIC -> getStaticFieldMark(context, insn as FieldInsnNode, use)

                in Opcodes.IADD..Opcodes.LXOR -> {
                    // handled in readSlotsConsumedByInsn
                }

                else -> createMarkForInstruction(context, insn, use)
            }
        }
    }

    private fun passArgumentsToCallee(
        context: InstrumentationContext,
        callee: MethodInsnNode,
        frame: Frame<AnalysisAbstractValue>,
    ) {
        val argumentCount = Type.getArgumentCount(callee.desc)
        val addend = if (callee.opcode == Opcodes.INVOKESTATIC) 0 else 1
        repeat(argumentCount) { i ->
            val value = frame.getStack(frame.stackSize - argumentCount + i)
            val slotIndex = context.slotIndexOf(value)
            if (slotIndex >= 0) {
                context.method.instructions.insertBefore(
                    callee,
                    instructionListOf(
                        IntInsnNode(Opcodes.BIPUSH, i + addend),
                        context.loadStateSlot(slotIndex),
                        trackerCall("setParameter", "(I${RUNTIME_STATE_DESC})V")
                    )
                )
            }
        }
    }

    private fun rememberInstanceFieldMark(
        context: InstrumentationContext,
        insn: FieldInsnNode,
        frame: Frame<AnalysisAbstractValue>,
    ) {
        val type = Type.getType(insn.desc)
        if (
            !validator.isApplicationField(insn.owner, insn.name) ||
            !isAllowedType(type)
        ) {
            return
        }

        val value = frame.getStack(frame.stackSize - 1)
        val index = context.slotIndexOf(value)
        if (index >= 0) {
            val freeLocal = with(context) {
                initialLocalsSized + max(0, frame.locals - initialLocalsSized)
            }
            val storeInsn = VarInsnNode(type.getOpcode(Opcodes.ISTORE), freeLocal)
            val loadInsn = VarInsnNode(type.getOpcode(Opcodes.ILOAD), freeLocal)
            context.fixups.add(storeInsn)
            context.fixups.add(loadInsn)
            context.method.instructions.insertBefore(
                insn,
                instructionListOf(
                    storeInsn,
                    InsnNode(Opcodes.DUP),
                    loadInsn
                )
            )
            context.method.instructions.insert(
                insn,
                instructionListOf(
                    context.loadStateSlot(index),
                    FieldInsnNode(
                        Opcodes.PUTFIELD,
                        insn.owner,
                        getGeneratedFieldName(insn.name),
                        RUNTIME_STATE_DESC
                    )
                )
            )
        }
    }

    private fun rememberStaticFieldMark(
        context: InstrumentationContext,
        insn: FieldInsnNode,
        frame: Frame<AnalysisAbstractValue>,
    ) {
        val type = Type.getType(insn.desc)
        if (
            !validator.isApplicationField(insn.owner, insn.name) ||
            !isAllowedType(type)
        ) {
            return
        }

        val value = frame.getStack(frame.stackSize - 1)
        val index = context.slotIndexOf(value)
        if (index >= 0) {
            context.method.instructions.insert(
                insn,
                instructionListOf(
                    context.loadStateSlot(index),
                    FieldInsnNode(
                        Opcodes.PUTSTATIC,
                        insn.owner,
                        getGeneratedFieldName(insn.name),
                        RUNTIME_STATE_DESC
                    )
                )
            )
        }
    }

    private fun passReturnToCaller(
        context: InstrumentationContext,
        insn: AbstractInsnNode,
        frame: Frame<AnalysisAbstractValue>,
    ) {
        val value = frame.getStack(frame.stackSize - 1)
        val index = context.slotIndexOf(value)
        if (index >= 0) {
            context.method.instructions.insertBefore(
                insn,
                instructionListOf(
                    context.loadStateSlot(index),
                    trackerCall("setReturnValue", "(${RUNTIME_STATE_DESC})V")
                )
            )
        }
    }

    private fun getReturnFromCallee(
        context: InstrumentationContext,
        callee: MethodInsnNode,
        use: AnalysisAbstractValue,
    ) {
        val index = context.allocate(use)
        context.method.instructions.insert(
            callee,
            instructionListOf(
                trackerCall("getReturnValue", "()${RUNTIME_STATE_DESC}"),
                context.storeStateSlot(index),
            )
        )
    }

    private fun getInstanceFieldMark(
        context: InstrumentationContext,
        insn: FieldInsnNode,
        use: AnalysisAbstractValue,
    ) {
        if (!validator.isApplicationField(insn.owner, insn.name)) {
            return
        }

        val index = context.allocate(use)
        context.method.instructions.insertBefore(
            insn,
            instructionListOf(
                InsnNode(Opcodes.DUP),
                FieldInsnNode(
                    Opcodes.GETFIELD,
                    insn.owner,
                    getGeneratedFieldName(insn.name),
                    RUNTIME_STATE_DESC
                ),
                context.storeStateSlot(index)
            )
        )
    }

    private fun getStaticFieldMark(
        context: InstrumentationContext,
        insn: FieldInsnNode,
        use: AnalysisAbstractValue,
    ) {
        if (!validator.isApplicationField(insn.owner, insn.name)) {
            return
        }

        val index = context.allocate(use)
        context.method.instructions.insertBefore(
            insn,
            instructionListOf(
                FieldInsnNode(
                    Opcodes.GETSTATIC,
                    insn.owner,
                    getGeneratedFieldName(insn.name),
                    RUNTIME_STATE_DESC
                ),
                context.storeStateSlot(index)
            )
        )
    }

    private fun createMarkForInstruction(
        context: InstrumentationContext,
        insn: AbstractInsnNode,
        use: AnalysisAbstractValue,
    ) {
        val index = context.allocate(use)
        val lineNumber = generateSequence(insn) { i -> i.previous }
            .filterIsInstance<LineNumberNode>()
            .firstOrNull()
            ?.line ?: -1
        val methodName = context.method.name
        context.method.instructions.insert(insn, context.storeStateSlot(index))
        context.method.instructions.insert(
            insn,
            newRuntimeState(describeInstruction(insn)),
        )
    }

    private fun allocateSlotsInLocals(
        context: InstrumentationContext,
    ) {
        var firstLabel: LabelNode? = null
        var lastLabel: LabelNode? = null
        for (instruction in context.originalInsns) {
            if (instruction is IincInsnNode) {
                if (instruction.`var` >= context.initialLocalsSized) {
                    instruction.`var` += context.slotsCount
                }
            } else if (instruction is VarInsnNode && instruction !in context.fixups) {
                if (instruction.`var` >= context.initialLocalsSized) {
                    instruction.`var` += context.slotsCount
                }
            } else if (instruction is FrameNode) {
                while (instruction.local.size < context.initialLocalsUnsized) {
                    instruction.local.add(Opcodes.TOP)
                }
                instruction.local.addAll(
                    context.initialLocalsUnsized,
                    List(context.slotsCount) { RUNTIME_STATE_NAME }
                )
            } else if (instruction is LabelNode) {
                if (firstLabel == null) {
                    firstLabel = instruction
                }
                lastLabel = instruction
            }
        }
        for (fixup in context.fixups) {
            fixup.`var` += context.slotsCount
        }
        for (local in context.method.localVariables.orEmpty()) {
            if (local.index >= context.initialLocalsSized) {
                local.index += context.slotsCount
            }
        }
        if (firstLabel != null && lastLabel != null) {
            if (context.method.localVariables == null) {
                context.method.localVariables = mutableListOf()
            }
            for (slotId in 0 until context.slotsCount) {
                context.method.localVariables.add(
                    LocalVariableNode(
                        "designAnalysisSlot$$slotId",
                        RUNTIME_STATE_DESC,
                        null,
                        firstLabel,
                        lastLabel,
                        context.initialLocalsSized + slotId
                    )
                )
            }
        }
    }

    private fun setInitialValueForInstructionSlots(context: InstrumentationContext) {
        repeat(context.slotsCount) { i ->
            context.method.instructions.insert(context.storeStateSlot(i))
            context.method.instructions.insert(InsnNode(Opcodes.ACONST_NULL))
        }
    }

    private fun instrumentMethodWithCounters(method: MethodNode, isTainted: Boolean) {
        if (isTainted) {
            method.instructions.insert(helperCall("incrementTaintedMethodCount"))
            method.instructions.insert(helperCall("incrementTotalMethodCount"))
        } else if (method.instructions.size() > 0) {
            method.instructions.insert(helperCall("incrementTotalMethodCount"))
        }
    }

    private fun calculateUsageInfo(
        methodNode: MethodNode,
        frames: Array<Frame<AnalysisAbstractValue>?>,
    ): UsageInfo {
        val argumentUses = mutableMapOf<Int, MutableSet<AnalysisAbstractValue>>()
        val instructionUses = mutableMapOf<AbstractInsnNode, MutableSet<AnalysisAbstractValue>>()
        val deathFrameIndex = mutableMapOf<AnalysisAbstractValue, Int>()
        calculateUsageInfo(methodNode, frames, argumentUses, instructionUses, deathFrameIndex)

        val dyingValuesAtFrame = mutableMapOf<Int, MutableSet<AnalysisAbstractValue>>()
        for ((value, insnIndex) in deathFrameIndex) {
            dyingValuesAtFrame.computeIfAbsent(insnIndex) { mutableSetOf() }.add(value)
        }

        return UsageInfo(
            argumentUses = argumentUses,
            instructionUses = instructionUses,
            dyingValuesAtFrame = dyingValuesAtFrame
        )
    }

    private fun calculateUsageInfo(
        method: MethodNode,
        frames: Array<Frame<AnalysisAbstractValue>?>,
        argumentUses: MutableMap<Int, MutableSet<AnalysisAbstractValue>>,
        instructionUses: MutableMap<AbstractInsnNode, MutableSet<AnalysisAbstractValue>>,
        deathInsnIndex: MutableMap<AnalysisAbstractValue, Int>
    ) {
        val visitor = FillUsesVisitor(
            method,
            argumentUses,
            instructionUses,
            deathInsnIndex
        )
        for (i in frames.indices.reversed()) {
            val frame = frames[i] ?: continue
            val insn = method.instructions.get(i)
            visitor.currentFrame = frame
            visitor.currentInsn = insn
            visitor.currentIndex = i
            insn.accept(visitor)
        }
    }

    private fun trackerCall(name: String, desc: String) = MethodInsnNode(
        Opcodes.INVOKESTATIC,
        RUNTIME_TRACKER_NAME,
        name,
        desc,
        false
    )

    private fun helperCall(name: String) = MethodInsnNode(
        Opcodes.INVOKESTATIC,
        RUNTIME_HELPER_NAME,
        name,
        "()V",
        false
    )

    private inner class PrimitivesAnalysisMethodVisitor(
        private val methodNode: MethodNode,
        private val delegate: MethodVisitor,
    ) : MethodVisitor(Opcodes.ASM9, methodNode) {
        override fun visitEnd() {
            val shouldInstrument = shouldInstrumentMethod(
                DeclaredMethod.make(name, methodNode.name, methodNode.desc)
            )
            if (shouldInstrument) {
                instrumentMethod(methodNode)
            }
            instrumentMethodWithCounters(methodNode, shouldInstrument)

            methodNode.accept(delegate)
        }
    }

    private inner class FillUsesVisitor(
        private val methodNode: MethodNode,
        private val argumentUses: MutableMap<Int, MutableSet<AnalysisAbstractValue>>,
        private val instructionUses: MutableMap<AbstractInsnNode, MutableSet<AnalysisAbstractValue>>,
        private val deathInsnIndex: MutableMap<AnalysisAbstractValue, Int>
    ) : MethodVisitor(Opcodes.ASM9) {
        lateinit var currentFrame: Frame<AnalysisAbstractValue>
        lateinit var currentInsn: AbstractInsnNode
        var currentIndex: Int = 0

        override fun visitInsn(opcode: Int) {
            val hasUses = !instructionUses[currentInsn].isNullOrEmpty()
            when (opcode) {
                in Opcodes.IRETURN..Opcodes.ARETURN -> {
                    val isTainted = isTainted(
                        MethodReturn(DeclaredMethod.make(name, methodNode.name, methodNode.desc))
                    )
                    if (isTainted) {
                        val value = currentFrame.getStack(currentFrame.stackSize - 1)
                        value.fillUses()
                    }
                }

                in Opcodes.INEG..Opcodes.DNEG -> {
                    if (hasUses) {
                        val value = currentFrame.getStack(currentFrame.stackSize - 1)
                        value.fillUses()
                    }
                }

                in Opcodes.IADD..Opcodes.LXOR -> {
                    if (hasUses) {
                        val value1 = currentFrame.getStack(currentFrame.stackSize - 1)
                        val value2 = currentFrame.getStack(currentFrame.stackSize - 2)
                        value1.fillUses()
                        value2.fillUses()
                    }
                }
            }
        }

        override fun visitFieldInsn(
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String
        ) {
            when (opcode) {
                Opcodes.PUTFIELD,
                Opcodes.PUTSTATIC -> {
                    val isTainted = isTainted(Field(DeclaredField(owner, name, descriptor)))
                    if (isTainted) {
                        val value = currentFrame.getStack(currentFrame.stackSize - 1)
                        value.fillUses()
                    }
                }
            }
        }

        override fun visitMethodInsn(
            opcode: Int,
            owner: String,
            name: String,
            descriptor: String,
            isInterface: Boolean
        ) {
            val argCount = Type.getArgumentCount(descriptor)
            for (i in 0 until argCount) {
                if (isTainted(MethodArgument(DeclaredMethod.make(owner, name, descriptor), i))) {
                    val value = currentFrame.getStack(currentFrame.stackSize - argCount + i)
                    value.fillUses()
                }
            }
        }

        private fun AnalysisAbstractValue.fillUses() {
            deathInsnIndex.compute(this) { _, old ->
                if (old == null || old < currentIndex) {
                    currentIndex
                } else {
                    old
                }
            }
            for (origin in origins) {
                when (origin) {
                    is ValueOrigin.Parameter -> argumentUses.computeIfAbsent(origin.local) {
                        mutableSetOf()
                    }.add(this)

                    is ValueOrigin.Insn -> instructionUses.computeIfAbsent(origin.insn) {
                        mutableSetOf()
                    }.add(this)
                }
            }
        }
    }

    companion object {
        private fun describeInstruction(instruction: AbstractInsnNode) = when (instruction.opcode) {
            Opcodes.ICONST_M1 -> "literal -1"
            Opcodes.ICONST_0 -> "literal 0"
            Opcodes.ICONST_1 -> "literal 1"
            Opcodes.ICONST_2 -> "literal 2"
            Opcodes.ICONST_3 -> "literal 3"
            Opcodes.ICONST_4 -> "literal 4"
            Opcodes.ICONST_5 -> "literal 5"
            Opcodes.LCONST_0 -> "literal 0L"
            Opcodes.LCONST_1 -> "literal 1L"
            Opcodes.FCONST_0 -> "literal 0.0F"
            Opcodes.FCONST_1 -> "literal 1.0F"
            Opcodes.FCONST_2 -> "literal 2.0F"
            Opcodes.DCONST_0 -> "literal 0.0"
            Opcodes.DCONST_1 -> "literal 1.0"

            Opcodes.BIPUSH,
            Opcodes.SIPUSH -> {
                instruction as IntInsnNode
                "literal ${instruction.operand}"
            }

            Opcodes.LDC -> {
                instruction as LdcInsnNode
                "literal ${instruction.cst}"
            }

            else -> instruction.toString()
        }
    }
}

private data class UsageInfo(
    val argumentUses: Map<Int, Set<AnalysisAbstractValue>>,
    val instructionUses: Map<AbstractInsnNode, Set<AnalysisAbstractValue>>,
    val dyingValuesAtFrame: Map<Int, Set<AnalysisAbstractValue>>,
)

private data class InstrumentationContext(
    val method: MethodNode,
    val originalInsns: List<AbstractInsnNode>,
    val initialLocalsUnsized: Int,
    val initialLocalsSized: Int,
    val argumentUses: Map<Int, Set<AnalysisAbstractValue>>,
    val instructionUses: Map<AbstractInsnNode, Set<AnalysisAbstractValue>>,
    val dyingValuesAtFrame: Map<Int, Set<AnalysisAbstractValue>>,
    val slots: MutableList<AnalysisAbstractValue?> = mutableListOf(),
    val fixups: MutableList<VarInsnNode> = mutableListOf(),
) {
    val slotsCount: Int
        get() = slots.size

    fun allocate(value: AnalysisAbstractValue): Int {
        var index = slots.indexOf(null)
        if (index == -1) {
            index = slots.size
            slots.add(value)
        } else {
            slots[index] = value
        }
        return index
    }

    fun releaseDyingAtFrame(frameIndex: Int) {
        val dying = dyingValuesAtFrame[frameIndex] ?: return
        for (value in dying) {
            val index = slots.indexOf(value)
            if (index >= 0) {
                slots[index] = null
            }
        }
    }

    fun slotIndexOf(value: AnalysisAbstractValue): Int {
        return slots.indexOf(value)
    }

    fun storeStateSlot(index: Int) = VarInsnNode(
        Opcodes.ASTORE,
        initialLocalsSized + index
    )

    fun loadStateSlot(index: Int) = VarInsnNode(
        Opcodes.ALOAD,
        initialLocalsSized + index
    )
}

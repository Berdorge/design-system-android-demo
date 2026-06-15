package com.vk.tools.designanalysis.instrumentation

import com.android.build.api.instrumentation.ClassContext
import com.vk.tools.designanalysis.RUNTIME_HELPER_NAME
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode

class ConcreteViewsVisitor(
    private val delegate: ClassVisitor,
    private val classContext: ClassContext,
) : ClassVisitor(Opcodes.ASM9, delegate) {
    private var access: Int = 0
    private var name: String = ""
    private var superName: String? = null
    private var hasOnTouchEvent = false
    private var hasOnDrawForeground = false
    private var hasGenerateLayoutParamsFromAttrs = false
    private var hasGenerateLayoutParamsFromLayoutParams = false

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String?>?
    ) {
        this.access = access
        this.name = name
        this.superName = superName
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String?>?
    ): MethodVisitor? {
        val delegate = super.visitMethod(access, name, descriptor, signature, exceptions)

        if (
            isViewSubclass() &&
            access and (Opcodes.ACC_ABSTRACT or Opcodes.ACC_INTERFACE) == 0 &&
            this.name != "com/vkui/analysis/DesignAnalysisOverlay"
        ) {
            if (name == "<init>") {
                val args = Type.getArgumentTypes(descriptor)
                val attributeSetIndex = args.indexOfFirst {
                    it.className == "android.util.AttributeSet"
                }
                return InstrumentViewConstructorVisitor(
                    Opcodes.ASM9,
                    delegate,
                    this.name,
                    this.superName,
                    attributeSetIndex
                )
            }
            if (name == "onTouchEvent" && descriptor == "(Landroid/view/MotionEvent;)Z") {
                hasOnTouchEvent = true
                return InstrumentOnTouchEventVisitor(Opcodes.ASM9, delegate)
            }
            if (name == "onDrawForeground" && descriptor == "(Landroid/graphics/Canvas;)V") {
                hasOnDrawForeground = true
                return InstrumentOnDrawForegroundVisitor(Opcodes.ASM9, delegate)
            }
            if (isViewGroupSubclass() && name == "generateLayoutParams") {
                if (descriptor.startsWith("(Landroid/util/AttributeSet;)")) {
                    hasGenerateLayoutParamsFromAttrs = true
                    return InstrumentGenerateLayoutParamsVisitor(
                        Opcodes.ASM9,
                        delegate,
                        LAYOUT_PARAMS_FROM_ATTRS_CAPTURE_DESC
                    )
                }
                if (descriptor.startsWith("(Landroid/view/ViewGroup\$LayoutParams;)")) {
                    hasGenerateLayoutParamsFromLayoutParams = true
                    return InstrumentGenerateLayoutParamsVisitor(
                        Opcodes.ASM9,
                        delegate,
                        LAYOUT_PARAMS_FROM_LAYOUT_PARAMS_CAPTURE_DESC
                    )
                }
            }
        }

        return delegate
    }

    override fun visitEnd() {
        if (isViewSubclass()) {
            if (!hasOnTouchEvent) {
                createOnTouchEventMethod()
            }
            if (!hasOnDrawForeground) {
                createOnDrawForegroundMethod()
            }
            if (isViewGroupSubclass()) {
                if (!hasGenerateLayoutParamsFromAttrs) {
                    createGenerateLayoutParams(
                        LAYOUT_PARAMS_FROM_ATTRS_DESC,
                        LAYOUT_PARAMS_FROM_ATTRS_CAPTURE_DESC
                    )
                }
                if (!hasGenerateLayoutParamsFromLayoutParams) {
                    createGenerateLayoutParams(
                        LAYOUT_PARAMS_FROM_LAYOUT_PARAMS_DESC,
                        LAYOUT_PARAMS_FROM_LAYOUT_PARAMS_CAPTURE_DESC
                    )
                }
            }
        }

        super.visitEnd()
    }

    private fun createOnTouchEventMethod() {
        val newMethod = MethodNode(
            Opcodes.ASM9,
            Opcodes.ACC_PUBLIC,
            "onTouchEvent",
            "(Landroid/view/MotionEvent;)Z",
            null,
            null
        )
        InstrumentOnTouchEventVisitor(api, newMethod).visitCode()
        newMethod.visitVarInsn(Opcodes.ALOAD, 0)
        newMethod.visitVarInsn(Opcodes.ALOAD, 1)
        newMethod.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            superName,
            "onTouchEvent",
            "(Landroid/view/MotionEvent;)Z",
            false
        )
        newMethod.visitInsn(Opcodes.IRETURN)
        newMethod.accept(delegate)
    }

    private fun createOnDrawForegroundMethod() {
        val newMethod = MethodNode(
            Opcodes.ASM9,
            Opcodes.ACC_PUBLIC,
            "onDrawForeground",
            "(Landroid/graphics/Canvas;)V",
            null,
            null
        )
        InstrumentOnDrawForegroundVisitor(api, newMethod).visitCode()
        newMethod.visitVarInsn(Opcodes.ALOAD, 0)
        newMethod.visitVarInsn(Opcodes.ALOAD, 1)
        newMethod.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            superName,
            "onDrawForeground",
            "(Landroid/graphics/Canvas;)V",
            false
        )
        newMethod.visitInsn(Opcodes.RETURN)
        newMethod.accept(delegate)
    }

    private fun createGenerateLayoutParams(
        descriptor: String,
        captureDescriptor: String,
    ) {
        val newMethod = MethodNode(
            Opcodes.ASM9,
            Opcodes.ACC_PROTECTED,
            "generateLayoutParams",
            descriptor,
            null,
            null
        )
        newMethod.visitVarInsn(Opcodes.ALOAD, 0)
        newMethod.visitVarInsn(Opcodes.ALOAD, 1)
        newMethod.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            superName,
            "generateLayoutParams",
            descriptor,
            false
        )
        InstrumentGenerateLayoutParamsVisitor(api, newMethod, captureDescriptor)
            .visitInsn(Opcodes.ARETURN)
        newMethod.accept(delegate)
    }

    private fun isViewSubclass(): Boolean {
        val superClasses = classContext.currentClassData.superClasses
        return "android.view.View" in superClasses
    }

    private fun isViewGroupSubclass(): Boolean {
        val superClasses = classContext.currentClassData.superClasses
        return "android.view.ViewGroup" in superClasses
    }

    companion object {
        private const val LAYOUT_PARAMS_FROM_ATTRS_DESC =
            "(Landroid/util/AttributeSet;)Landroid/view/ViewGroup\$LayoutParams;"
        private const val LAYOUT_PARAMS_FROM_ATTRS_CAPTURE_DESC =
            "(Landroid/view/ViewGroup\$LayoutParams;Landroid/view/ViewGroup;Landroid/util/AttributeSet;)V"
        private const val LAYOUT_PARAMS_FROM_LAYOUT_PARAMS_DESC =
            "(Landroid/view/ViewGroup\$LayoutParams;)Landroid/view/ViewGroup\$LayoutParams;"
        private const val LAYOUT_PARAMS_FROM_LAYOUT_PARAMS_CAPTURE_DESC =
            "(Landroid/view/ViewGroup\$LayoutParams;Landroid/view/ViewGroup;Landroid/view/ViewGroup\$LayoutParams;)V"
    }
}

private class InstrumentViewConstructorVisitor(
    api: Int,
    delegate: MethodVisitor?,
    private val className: String,
    private val superName: String?,
    private val attributeSetIndex: Int,
) : MethodVisitor(api, delegate) {
    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.RETURN) {
            super.visitVarInsn(Opcodes.ALOAD, 0)
            super.visitInsn(Opcodes.ICONST_0)
            super.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                className,
                "setWillNotDraw",
                "(Z)V",
                false
            )
        }
        super.visitInsn(opcode)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String,
        isInterface: Boolean
    ) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        if (
            opcode == Opcodes.INVOKESPECIAL &&
            name == "<init>" &&
            owner == superName &&
            descriptor.contains("Landroid/util/AttributeSet;") &&
            attributeSetIndex >= 0
        ) {
            super.visitVarInsn(Opcodes.ALOAD, 0)
            super.visitVarInsn(Opcodes.ALOAD, attributeSetIndex + 1)
            super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                RUNTIME_HELPER_NAME,
                "captureViewAttributes",
                "(Landroid/view/View;Landroid/util/AttributeSet;)V",
                false
            )
        }
    }
}

private class InstrumentOnTouchEventVisitor(
    api: Int,
    delegate: MethodVisitor?
) : MethodVisitor(api, delegate) {
    override fun visitCode() {
        super.visitCode()

        val beginLabel = Label()
        super.visitVarInsn(Opcodes.ALOAD, 0)
        super.visitVarInsn(Opcodes.ALOAD, 1)
        super.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            RUNTIME_HELPER_NAME,
            "onTouchEvent",
            "(Landroid/view/View;Landroid/view/MotionEvent;)Z",
            false
        )
        super.visitJumpInsn(Opcodes.IFEQ, beginLabel)
        super.visitInsn(Opcodes.ICONST_1)
        super.visitInsn(Opcodes.IRETURN)
        super.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
        super.visitLabel(beginLabel)
    }
}

private class InstrumentOnDrawForegroundVisitor(
    api: Int,
    delegate: MethodVisitor?
) : MethodVisitor(api, delegate) {
    override fun visitCode() {
        super.visitCode()

        super.visitVarInsn(Opcodes.ALOAD, 0)
        super.visitVarInsn(Opcodes.ALOAD, 1)
        super.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            RUNTIME_HELPER_NAME,
            "onDrawForeground",
            "(Landroid/view/View;Landroid/graphics/Canvas;)V",
            false
        )
    }
}

private class InstrumentGenerateLayoutParamsVisitor(
    api: Int,
    delegate: MethodVisitor?,
    private val captureDescriptor: String,
) : MethodVisitor(api, delegate) {
    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.ARETURN) {
            super.visitInsn(Opcodes.DUP)
            super.visitVarInsn(Opcodes.ALOAD, 0)
            super.visitVarInsn(Opcodes.ALOAD, 1)
            super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                RUNTIME_HELPER_NAME,
                "captureLayoutParams",
                captureDescriptor,
                false
            )
        }
        super.visitInsn(opcode)
    }
}

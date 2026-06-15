package com.vk.tools.designanalysis.instrumentation

import com.android.build.api.instrumentation.ClassContext
import com.vk.tools.designanalysis.RUNTIME_HELPER_NAME
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.INVOKESTATIC

class ModelledReplacementVisitor(
    delegate: ClassVisitor,
    private val classContext: ClassContext,
) : ClassVisitor(Opcodes.ASM9, delegate) {
    private var className: String = ""
    private var superName: String? = null
    private val replacedMethods = listOf(
        ReplacedMethod(
            "android/content/res/Resources\$Theme",
            "resolveAttribute",
            "(ILandroid/util/TypedValue;Z)Z"
        ),
        ReplacedMethod(
            "android/content/res/Resources",
            "getDimension",
            "(I)F"
        ),
        ReplacedMethod(
            "android/content/res/Resources",
            "getDimensionPixelSize",
            "(I)I"
        ),
        ReplacedMethod(
            "android/content/res/Resources",
            "getDimensionPixelOffset",
            "(I)I"
        ),
        ReplacedMethod(
            "android/util/TypedValue",
            "getFloat",
            "()F"
        ),
        ReplacedMethod(
            "android/util/TypedValue",
            "complexToDimension",
            "(ILandroid/util/DisplayMetrics;)F",
        ),
        ReplacedMethod(
            "android/util/TypedValue",
            "complexToDimensionPixelSize",
            "(ILandroid/util/DisplayMetrics;)I",
        ),
        ReplacedMethod(
            "android/util/TypedValue",
            "complexToDimensionPixelOffset",
            "(ILandroid/util/DisplayMetrics;)I",
        ),
        ReplacedMethod(
            "android/content/res/TypedArray",
            "getDimensionPixelSize",
            "(II)I"
        ),
        ReplacedMethod(
            "android/content/res/TypedArray",
            "getDimensionPixelOffset",
            "(II)I"
        ),
        ReplacedMethod(
            "android/content/Context",
            "obtainStyledAttributes",
            "(Landroid/util/AttributeSet;[I)Landroid/content/res/TypedArray;"
        ),
        ReplacedMethod(
            "android/content/Context",
            "obtainStyledAttributes",
            "(Landroid/util/AttributeSet;[III)Landroid/content/res/TypedArray;"
        ),
        ReplacedMethod("android/view/View", "getPaddingLeft", "()I"),
        ReplacedMethod("android/view/View", "getPaddingStart", "()I"),
        ReplacedMethod("android/view/View", "getPaddingTop", "()I"),
        ReplacedMethod("android/view/View", "getPaddingRight", "()I"),
        ReplacedMethod("android/view/View", "getPaddingEnd", "()I"),
        ReplacedMethod("android/view/View", "getPaddingBottom", "()I"),
        ReplacedMethod("android/view/View", "setPadding", "(IIII)V"),
        ReplacedMethod("android/view/View", "setPaddingRelative", "(IIII)V"),
        ReplacedMethod("android/view/ViewGroup\$MarginLayoutParams", "setMargins", "(IIII)V"),
        ReplacedMethod("android/view/ViewGroup\$MarginLayoutParams", "setMarginStart", "(I)V"),
        ReplacedMethod("android/view/ViewGroup\$MarginLayoutParams", "setMarginEnd", "(I)V"),
        ReplacedMethod("android/view/ViewGroup\$MarginLayoutParams", "getMarginStart", "()I"),
        ReplacedMethod("android/view/ViewGroup\$MarginLayoutParams", "getMarginEnd", "()I"),
    )
    private val replacedFields = listOf(
        ReplacedField(
            "android/util/TypedValue",
            "data",
            "I"
        ),
        ReplacedField(
            "android/view/ViewGroup\$MarginLayoutParams",
            "leftMargin",
            "I"
        ),
        ReplacedField(
            "android/view/ViewGroup\$MarginLayoutParams",
            "topMargin",
            "I"
        ),
        ReplacedField(
            "android/view/ViewGroup\$MarginLayoutParams",
            "rightMargin",
            "I"
        ),
        ReplacedField(
            "android/view/ViewGroup\$MarginLayoutParams",
            "bottomMargin",
            "I"
        ),
    )

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.className = name
        this.superName = superName
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        val delegate = super.visitMethod(access, name, descriptor, signature, exceptions)
        return if ("com/vkui/analysis/runtime" in className) {
            delegate
        } else {
            ModelledReplacementMethodVisitor(
                api = api,
                name = name,
                descriptor = descriptor,
                delegate = delegate
            )
        }
    }

    private inner class ModelledReplacementMethodVisitor(
        api: Int,
        private val name: String,
        private val descriptor: String,
        private val delegate: MethodVisitor?,
    ) : MethodVisitor(api, delegate) {
        override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
            val replacedField = replacedFields.find { field ->
                field.name == name && field.descriptor == descriptor && run {
                    val classData = classContext.loadClassData(owner.replace('/', '.'))
                    val superClasses = classData?.superClasses.orEmpty()
                    field.owner == owner || field.owner.replace('/', '.') in superClasses
                }
            }
            if (replacedField == null) {
                super.visitFieldInsn(opcode, owner, name, descriptor)
                return
            }

            val name = when (opcode) {
                Opcodes.GETFIELD, Opcodes.GETSTATIC -> "get${name.capitalize()}"
                else -> "set${name.capitalize()}"
            }
            val descriptor = when (opcode) {
                Opcodes.GETSTATIC -> "()$descriptor"
                Opcodes.GETFIELD -> "(L${replacedField.owner};)$descriptor"
                Opcodes.PUTSTATIC -> "($descriptor)V"
                else -> "(L${replacedField.owner};$descriptor)V"
            }
            super.visitMethodInsn(INVOKESTATIC, RUNTIME_HELPER_NAME, name, descriptor, false)
        }

        override fun visitMethodInsn(
            opcode: Int,
            owner: String,
            name: String?,
            descriptor: String,
            isInterface: Boolean
        ) {
            val isSuperCall = opcode == Opcodes.INVOKESPECIAL
                && superName == owner
                && this.name == name
                && this.descriptor.substringBeforeLast(')') == descriptor.substringBeforeLast(')')
            if (isSuperCall) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                return
            }

            val replacedMethod = replacedMethods.find { method ->
                method.name == name && method.descriptor == descriptor && run {
                    val classData = classContext.loadClassData(owner.replace('/', '.'))
                    val superClasses = classData?.superClasses.orEmpty()
                    val interfaces = classData?.interfaces.orEmpty()
                    method.owner == owner
                        || method.owner.replace('/', '.') in superClasses
                        || method.owner.replace('/', '.') in interfaces
                }
            }
            if (replacedMethod == null) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                return
            }

            val descriptor = when (opcode) {
                INVOKESTATIC -> descriptor
                else -> "(L${replacedMethod.owner};${descriptor.substring(1)}"
            }
            super.visitMethodInsn(
                INVOKESTATIC,
                RUNTIME_HELPER_NAME,
                name,
                descriptor,
                false
            )
        }
    }
}

private data class ReplacedMethod(
    val owner: String,
    val name: String,
    val descriptor: String,
)

private data class ReplacedField(
    val owner: String,
    val name: String,
    val descriptor: String
)

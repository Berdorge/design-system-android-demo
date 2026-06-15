package com.vk.tools.designanalysis

import org.objectweb.asm.ClassVisitor

class ClassVisitorChain private constructor(
    firstFactory: (ClassVisitor) -> ClassVisitor
) {
    val currentChain = mutableListOf(firstFactory)

    fun then(factory: (ClassVisitor) -> ClassVisitor) = apply {
        currentChain.add(factory)
    }

    fun finally(root: ClassVisitor): ClassVisitor {
        var current: ClassVisitor = root
        for (factory in currentChain.asReversed()) {
            current = factory(current)
        }
        return current
    }

    companion object {
        fun first(factory: (ClassVisitor) -> ClassVisitor) = ClassVisitorChain(factory)

        /**
         * For simplicity of commenting out some visitors
         * inside a chain of [then] calls.
         */
        fun then(factory: (ClassVisitor) -> ClassVisitor) = first(factory)

        /**
         * For simplicity of commenting out the whole chain of
         * [then] calls.
         */
        fun finally(root: ClassVisitor) = root
    }
}

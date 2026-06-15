package com.vkui.analysis.test.mock

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData

class TestClassContext(
    private val classLoader: TestClassLoader,
    private val clazz: Class<*>
) : ClassContext {
    override val currentClassData: ClassData = object : ClassData {
        override val className: String = clazz.name

        override val classAnnotations: List<String> = emptyList()

        override val superClasses: List<String> = generateSequence(clazz.superclass) {
            it.superclass
        }
            .mapTo(mutableListOf()) { it.name }

        override val interfaces: List<String> = generateSequence(clazz) {
            it.superclass
        }
            .flatMapTo(mutableSetOf()) { it.interfaces.asIterable() }
            .map { it.name }
    }

    override fun loadClassData(className: String): ClassData? {
        return classLoader.loadClass(className)?.let { clazz ->
            TestClassContext(classLoader, clazz).currentClassData
        }
    }
}

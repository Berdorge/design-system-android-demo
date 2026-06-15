package com.vkui.analysis.test.mock

import com.vk.tools.designanalysis.hierarchy.ClassHierarchy
import com.vk.tools.designanalysis.hierarchy.DeclaredField
import com.vk.tools.designanalysis.hierarchy.DeclaredMethod

class TestClassHierarchy : ClassHierarchy {
    override fun getSuperclassOf(className: String): String? {
        val classLoader = Thread.currentThread().contextClassLoader
        val loadedClass = classLoader.loadClass(className.replace('/', '.'))
        return loadedClass?.superclass?.name?.replace('.', '/')
    }

    override fun getOverriddenMethod(method: DeclaredMethod): DeclaredMethod? {
        return null
    }

    override fun getPossibleTargets(method: DeclaredMethod): Set<DeclaredMethod> {
        return setOf(method)
    }

    override fun getPossibleTargets(field: DeclaredField): Set<DeclaredField> {
        return setOf(field)
    }

    override fun getDeclaredMethods(className: String): Collection<DeclaredMethod> {
        return emptyList()
    }
}

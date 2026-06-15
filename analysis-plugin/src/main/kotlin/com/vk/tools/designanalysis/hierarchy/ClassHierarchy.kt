package com.vk.tools.designanalysis.hierarchy

interface ClassHierarchy {
    fun getSuperclassOf(
        className: String
    ): String?

    fun getPossibleTargets(
        method: DeclaredMethod
    ): Set<DeclaredMethod>

    fun getPossibleTargets(
        field: DeclaredField
    ): Set<DeclaredField>

    fun getDeclaredMethods(
        className: String
    ): Collection<DeclaredMethod>

    fun getOverriddenMethod(
        method: DeclaredMethod
    ): DeclaredMethod?
}

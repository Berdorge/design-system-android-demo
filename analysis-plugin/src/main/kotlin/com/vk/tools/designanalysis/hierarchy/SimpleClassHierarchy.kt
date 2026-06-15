package com.vk.tools.designanalysis.hierarchy

class SimpleClassHierarchy : ClassHierarchy {
    private val superclassOf = mutableMapOf<String, String>()
    private val subclassesOf = mutableMapOf<String, MutableSet<String>>()
    private val implementors = mutableMapOf<String, MutableSet<String>>()
    private val declaredMethods = mutableMapOf<String, DeclaredMethods>()
    private val declaredFields = mutableMapOf<String, DeclaredFields>()

    private val cache: MutableMap<DeclaredMethod, Set<DeclaredMethod>> = mutableMapOf()

    override fun getSuperclassOf(className: String): String? {
        return superclassOf[className]
    }

    override fun getPossibleTargets(method: DeclaredMethod): Set<DeclaredMethod> {
        cache[method]?.let {
            return it
        }
        val result = mutableSetOf<DeclaredMethod>()
        generateSequence(superclassOf[method.className]) { superclassOf[it] }.firstNotNullOfOrNull {
            declaredMethods[it]?.tryResolveCall(method)
        }?.let(result::add)
        getPossibleTargetsRecursive(method.className, method, result)
        cache[method] = result
        return result
    }

    override fun getPossibleTargets(field: DeclaredField): Set<DeclaredField> {
        val result = mutableSetOf<DeclaredField>()
        generateSequence(superclassOf[field.className]) { superclassOf[it] }.firstNotNullOfOrNull {
            declaredFields[it]?.tryResolveAccess(field)
        }?.let(result::add)
        getPossibleTargetsRecursive(field.className, field, result)
        return result
    }

    override fun getOverriddenMethod(method: DeclaredMethod): DeclaredMethod? {
        return generateSequence(superclassOf[method.className]) {
            superclassOf[it]
        }.firstNotNullOfOrNull {
            declaredMethods[it]?.tryResolveCall(method)
        }
    }

    override fun getDeclaredMethods(className: String): Collection<DeclaredMethod> {
        return declaredMethods[className]?.getAll().orEmpty()
    }

    fun setSuperclassOf(subclass: String, superclass: String) {
        superclassOf.compute(subclass) { _, oldSuperclass ->
            check(oldSuperclass == null || oldSuperclass == superclass) {
                "Class $subclass already has superclass $oldSuperclass, got $superclass"
            }
            superclass
        }
        subclassesOf.computeIfAbsent(superclass) { mutableSetOf() }.add(subclass)
    }

    fun setImplementedOf(implementor: String, implemented: String) {
        implementors.computeIfAbsent(implemented) { mutableSetOf() }.add(implementor)
    }

    fun addDeclaredMethod(method: DeclaredMethod) {
        declaredMethods.computeIfAbsent(method.className) { DeclaredMethods() }.add(method)
    }

    fun getAllSuperclassRelations() = superclassOf

    fun addDeclaredField(field: DeclaredField) {
        declaredFields.computeIfAbsent(field.className) { DeclaredFields() }.add(field)
    }

    fun getAllDeclaredFields() = declaredFields

    fun clear() {
        superclassOf.clear()
        subclassesOf.clear()
        implementors.clear()
        declaredMethods.clear()
        declaredFields.clear()
        cache.clear()
    }

    private fun getPossibleTargetsRecursive(
        className: String,
        method: DeclaredMethod,
        result: MutableSet<DeclaredMethod>
    ) {
        val overridden = declaredMethods[className]?.tryResolveCall(method)
        if (overridden != null) {
            result.add(overridden)
        }
        for (implementor in implementors[className].orEmpty()) {
            getPossibleTargetsRecursive(implementor, method, result)
        }
        for (subclass in subclassesOf[className].orEmpty()) {
            getPossibleTargetsRecursive(subclass, method, result)
        }
    }

    private fun getPossibleTargetsRecursive(
        className: String,
        field: DeclaredField,
        result: MutableSet<DeclaredField>
    ) {
        val overridden = declaredFields[className]?.tryResolveAccess(field)
        if (overridden != null) {
            result.add(overridden)
        }
        for (subclass in subclassesOf[className].orEmpty()) {
            getPossibleTargetsRecursive(subclass, field, result)
        }
    }
}

package com.vk.tools.designanalysis.hierarchy

@JvmInline
internal value class DeclaredMethods(
    private val methodsByNameWithArguments: HashMap<String, DeclaredMethod> = hashMapOf()
) {
    fun add(method: DeclaredMethod) {
        methodsByNameWithArguments.compute(method.nameWithArgumentsDescriptor) { _, oldMethod ->
            check(
                oldMethod == null ||
                    oldMethod.nameWithArgumentsDescriptor == method.nameWithArgumentsDescriptor
            ) {
                "Method ${method.nameWithArgumentsDescriptor} already declared: $oldMethod, got $method"
            }
            method
        }
    }

    fun tryResolveCall(method: DeclaredMethod): DeclaredMethod? {
        return methodsByNameWithArguments[method.nameWithArgumentsDescriptor]
    }

    fun getAll(): Collection<DeclaredMethod> = methodsByNameWithArguments.values
}

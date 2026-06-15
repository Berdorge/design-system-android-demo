package com.vk.tools.designanalysis.hierarchy

@JvmInline
value class DeclaredFields(
    private val fieldsByName: HashMap<String, DeclaredField> = hashMapOf()
) {
    fun add(field: DeclaredField) {
        fieldsByName.compute(field.name) { _, oldField ->
            check(
                oldField == null ||
                    oldField.name == field.name
            ) {
                "Field ${field.name} already declared: $oldField, got $field"
            }
            field
        }
    }

    fun tryResolveAccess(field: DeclaredField): DeclaredField? {
        return fieldsByName[field.name]
    }

    fun get(fieldName: String): DeclaredField? {
        return fieldsByName[fieldName]
    }
}

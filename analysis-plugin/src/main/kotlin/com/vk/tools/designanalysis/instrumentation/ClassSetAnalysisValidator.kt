package com.vk.tools.designanalysis.instrumentation

import com.vk.tools.designanalysis.hierarchy.DeclaredFields

internal class ClassSetAnalysisValidator(
    private val superclassOf: Map<String, String>,
    private val declaredFields: Map<String, DeclaredFields>
) : AnalysisValidator {
    override fun isApplicationField(
        className: String,
        fieldName: String
    ): Boolean {
        val result = generateSequence(className) { superclassOf[it] }.any { clazz ->
            declaredFields[clazz]?.get(fieldName) != null
        }
        return result
    }
}

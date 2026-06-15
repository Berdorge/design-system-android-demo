package com.vk.tools.designanalysis.instrumentation

interface AnalysisValidator {
    fun isApplicationField(
        className: String,
        fieldName: String
    ): Boolean
}

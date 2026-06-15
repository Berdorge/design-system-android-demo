package com.vkui.analysis.test.mock

import com.vk.tools.designanalysis.instrumentation.AnalysisValidator

class TestAnalysisValidator : AnalysisValidator {
    override fun isApplicationField(
        className: String,
        fieldName: String,
    ): Boolean {
        return !className.startsWith("android/")
    }
}

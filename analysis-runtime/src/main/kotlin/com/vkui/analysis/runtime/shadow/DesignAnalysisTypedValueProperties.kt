package com.vkui.analysis.runtime.shadow

import android.util.TypedValue
import com.vkui.analysis.runtime.DesignAnalysisMark

internal class DesignAnalysisTypedValueProperties : TypedValue() {
    @JvmField
    var dataState: DesignAnalysisMark? = null
}

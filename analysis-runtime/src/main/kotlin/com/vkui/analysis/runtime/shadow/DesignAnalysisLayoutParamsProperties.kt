package com.vkui.analysis.runtime.shadow

import com.vkui.analysis.runtime.DesignAnalysisMark

data class DesignAnalysisLayoutParamsProperties(
    var marginStart: DesignAnalysisMark? = null,
    var marginTop: DesignAnalysisMark? = null,
    var marginEnd: DesignAnalysisMark? = null,
    var marginBottom: DesignAnalysisMark? = null,
)

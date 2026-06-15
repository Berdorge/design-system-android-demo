package com.vkui.analysis.runtime.shadow

import com.vkui.analysis.runtime.DesignAnalysisMark

data class DesignAnalysisViewProperties(
    var paddingStart: DesignAnalysisMark? = null,
    var paddingTop: DesignAnalysisMark? = null,
    var paddingEnd: DesignAnalysisMark? = null,
    var paddingBottom: DesignAnalysisMark? = null,
)

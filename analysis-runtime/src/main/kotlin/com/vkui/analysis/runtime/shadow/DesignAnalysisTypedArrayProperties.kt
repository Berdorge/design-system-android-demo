package com.vkui.analysis.runtime.shadow

import android.content.Context
import android.util.AttributeSet

data class DesignAnalysisTypedArrayProperties(
    val context: Context,
    val attributeSet: AttributeSet,
    val attrs: IntArray,
)

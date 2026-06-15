package com.vkui.analysis.runtime

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.vkui.analysis.runtime.view.DesignAnalysisFrameLayout
import com.vkui.analysis.runtime.view.DesignAnalysisTextView

class DesignAnalysisLayoutInflaterFactory(
    private val layoutInflater: LayoutInflater
) : LayoutInflater.Factory2 {
    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        val isCustomView = name.contains('.')
        val view = when {
            isCustomView -> layoutInflater.createView(context, name, null, attrs)
            name == "FrameLayout" -> return DesignAnalysisFrameLayout(context, attrs)
            name == "TextView" -> return DesignAnalysisTextView(context, attrs)
            else -> layoutInflater.onCreateView(context, parent, name, attrs)
        }
        return view
    }
}

package com.vkui.view.theme

import android.content.Context
import android.util.AttributeSet
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import androidx.core.util.isNotEmpty
import com.vkui.demo.R
import com.vkui.view.theme.VkThemeHelper.ThemedAttribute

class VkLayoutInflaterFactory(
    private val layoutInflater: LayoutInflater
) : LayoutInflater.Factory2 {
    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        val isCustomView = name.contains('.')
        val view = if (isCustomView) {
            layoutInflater.createView(context, name, null, attrs)
        } else {
            layoutInflater.onCreateView(context, parent, name, attrs)
        }
        if (view != null) {
            val themeTag = createThemeTag(attrs)
            if (themeTag.isNotEmpty()) {
                view.setTag(R.id.theme_tag, themeTag)
            }
        }
        return view
    }

    private fun createThemeTag(attrs: AttributeSet): SparseIntArray {
        val result = SparseIntArray()
        ifAttributePresent(attrs, "textColor") { attrId ->
            result.put(ThemedAttribute.TEXT_COLOR.ordinal, attrId)
        }
        ifAttributePresent(attrs, "background") { attrId ->
            result.put(ThemedAttribute.BACKGROUND_COLOR.ordinal, attrId)
        }
        return result
    }

    private inline fun ifAttributePresent(
        attrs: AttributeSet,
        propertyName: String,
        action: (Int) -> Unit
    ) {
        val str = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", propertyName)
        if (str != null && str.startsWith("?")) {
            val attrId = str.substring(1).toInt()
            action(attrId)
        }
    }
}

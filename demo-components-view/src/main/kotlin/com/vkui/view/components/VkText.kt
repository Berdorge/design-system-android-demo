package com.vkui.view.components

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class VkText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    init {
        includeFontPadding = false
    }
}

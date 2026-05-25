package com.vkui.view.components

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.use
import androidx.core.view.updatePaddingRelative
import com.vkui.view.resolveColor
import com.vkui.view.resolveDimensionPixelSize
import com.vkui.view.resolveDrawable
import com.vkui.view.resolveTextAppearance

class VkButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val textView: TextView

    init {
        background = context.resolveDrawable(androidx.appcompat.R.attr.selectableItemBackground)

        textView = TextView(context).apply {
            layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER)
            setTextAppearance(context.resolveTextAppearance(R.attr.vk_ui_font_headline2))
            setTextColor(context.resolveColor(R.attr.vk_ui_color_text_accent))
        }
        addView(textView)

        context.obtainStyledAttributes(attrs, R.styleable.VkButton).use { typedArray ->
            setText(typedArray.getText(R.styleable.VkButton_android_text))
            val horizontalPadding = typedArray.getDimensionPixelSize(
                R.styleable.VkButton_randomButtonAttribute,
                context.resolveDimensionPixelSize(R.attr.vk_ui_spacing_size_xl)
            )
            minimumHeight = typedArray.getDimensionPixelSize(
                R.styleable.VkButton_android_minHeight,
                context.resolveDimensionPixelSize(R.attr.vk_ui_size_button_medium_height)
            )
            updatePaddingRelative(start = horizontalPadding, end = horizontalPadding)
        }
    }

    fun setText(text: CharSequence?) {
        textView.text = text
    }
}

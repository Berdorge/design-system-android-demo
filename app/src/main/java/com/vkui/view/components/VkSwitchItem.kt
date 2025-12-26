package com.vkui.view.components

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.use
import androidx.core.view.isVisible
import com.vkui.demo.R
import com.vkui.view.getEnum
import com.vkui.view.resolveDimensionPixelSize
import com.vkui.view.resolveFloat

class VkSwitchItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val titleView: TextView
    private val subtitleView: TextView
    private val switchView: VkSwitch

    var switchAlignment: SwitchAlignment = SwitchAlignment.END
        set(value) {
            field = value
            updateSwitchAlignment(value)
        }

    init {
        val horizontalPadding = context.resolveDimensionPixelSize(R.attr.vk_ui_size_base_padding_horizontal)
        val verticalPadding = context.resolveDimensionPixelSize(R.attr.vk_ui_spacing_size_l)
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

        inflate(context, R.layout.vk_switch_item, this)
        titleView = findViewById(R.id.title)
        subtitleView = findViewById(R.id.subtitle)
        switchView = findViewById(R.id.sub_switch)

        context.obtainStyledAttributes(attrs, R.styleable.VkSwitchItem).use { attrs ->
            setTitle(attrs.getString(R.styleable.VkSwitchItem_switchItemTitle))
            setSubtitle(attrs.getString(R.styleable.VkSwitchItem_switchItemSubtitle))
            switchAlignment = attrs.getEnum(R.styleable.VkSwitchItem_switchAlignment, switchAlignment)
            clipChildren = false
            clipToPadding = false
        }

        setOnClickListener {
            switchView.isSelected = !switchView.isSelected
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        switchView.isEnabled = enabled
        switchView.alpha = 1F
        alpha = if (enabled) {
            1F
        } else {
            context.resolveFloat(R.attr.vk_ui_opacity_disable)
        }
    }

    fun setTitle(title: String?) {
        titleView.text = title
        titleView.isVisible = title != null
    }

    fun setSubtitle(subtitle: String?) {
        subtitleView.text = subtitle
        subtitleView.isVisible = subtitle != null
    }

    fun setOnSelectedStateChangedListener(listener: (() -> Unit)?) {
        switchView.setOnSelectedStateChangedListener(listener)
    }

    private fun updateSwitchAlignment(alignment: SwitchAlignment) {
        val gap = context.resolveDimensionPixelSize(R.attr.vk_ui_spacing_size_xl)
        val order = when (alignment) {
            SwitchAlignment.START -> intArrayOf(R.id.sub_switch, R.id.text_space)
            SwitchAlignment.END -> intArrayOf(R.id.text_space, R.id.sub_switch)
        }
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        constraintSet.createHorizontalChainRtl(
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            order,
            null,
            ConstraintSet.CHAIN_PACKED
        )
        constraintSet.setMargin(order[0], ConstraintSet.END, gap)
        constraintSet.setMargin(order[1], ConstraintSet.START, 0)
        constraintSet.applyTo(this)
    }

    enum class SwitchAlignment {
        START,
        END
    }
}

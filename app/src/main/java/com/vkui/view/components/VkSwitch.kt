package com.vkui.view.components

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Switch
import com.vkui.demo.R
import com.vkui.view.Screen
import com.vkui.view.Screen.dpToPx
import com.vkui.view.Themable
import com.vkui.view.resolveColor
import com.vkui.view.resolveDimensionPixelSize
import com.vkui.view.resolveFloat

class VkSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ValueAnimator.AnimatorUpdateListener, Themable {
    private val track = View(context)
    private val thumb = View(context)
    private val trackDrawable = ShapeDrawable(completelyRoundRectShape(context))
    private val thumbDrawable = ShapeDrawable(completelyRoundRectShape(context))
    private val argbEvaluator = ArgbEvaluator()
    private val checkedAnimator = ValueAnimator.ofFloat(0F, 1F)
    private val checkedThumbOffset: Int

    private var selectedStateChangedListener: (() -> Unit)? = null

    init {
        val trackPadding = context.resolveDimensionPixelSize(R.attr.vk_ui_spacing_size2_xs)
        val switchWidth = context.resolveDimensionPixelSize(R.attr.vk_ui_size_switch_width)
            .plus(trackPadding * 2)
        val switchHeight = context.resolveDimensionPixelSize(R.attr.vk_ui_size_switch_height)
        val thumbSize = context.resolveDimensionPixelSize(R.attr.vk_ui_size_switch_pin)
        checkedThumbOffset = switchWidth - thumbSize

        val layerTrackDrawable = LayerDrawable(arrayOf(trackDrawable))
        layerTrackDrawable.setLayerInsetStart(0, trackPadding)
        layerTrackDrawable.setLayerInsetEnd(0, trackPadding)
        track.background = layerTrackDrawable
        addView(track, LayoutParams(switchWidth, switchHeight, Gravity.CENTER_VERTICAL))

        thumb.background = thumbDrawable
        thumb.elevation = 1F.dpToPx(context)
        addView(thumb, LayoutParams(thumbSize, thumbSize, Gravity.CENTER_VERTICAL))

        checkedAnimator.setDuration(200L)
        checkedAnimator.addUpdateListener(this)
        onAnimationUpdate(checkedAnimator)

        setOnClickListener {
            isSelected = !isSelected
        }
    }

    override fun changeTheme() {
        onAnimationUpdate(checkedAnimator)
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val uncheckedTrackColor = context.resolveColor(R.attr.vk_ui_color_icon_tertiary_alpha)
        val checkedTrackColor = context.resolveColor(R.attr.vk_ui_color_background_accent_themed_alpha)
        val uncheckedThumbColor = context.resolveColor(R.attr.vk_ui_color_icon_contrast_secondary)
        val checkedThumbColor = context.resolveColor(R.attr.vk_ui_color_icon_accent)
        val progress = animation.animatedValue as Float
        val trackColor = argbEvaluator.evaluate(
            progress,
            uncheckedTrackColor,
            checkedTrackColor
        ) as Int
        val thumbColor = argbEvaluator.evaluate(
            progress,
            uncheckedThumbColor,
            checkedThumbColor
        ) as Int
        val thumbOffset = (checkedThumbOffset * progress).toInt()

        trackDrawable.paint.color = trackColor
        thumbDrawable.paint.color = thumbColor
        trackDrawable.invalidateSelf()
        thumbDrawable.invalidateSelf()
        thumb.translationX = thumbOffset.toFloat()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) {
            1F
        } else {
            context.resolveFloat(R.attr.vk_ui_opacity_disable)
        }
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            checkedAnimator.start()
        } else {
            checkedAnimator.reverse()
        }
        selectedStateChangedListener?.invoke()
    }

    override fun getAccessibilityClassName() = Switch::class.java.name

    fun setOnSelectedStateChangedListener(listener: (() -> Unit)?) {
        selectedStateChangedListener = listener
    }

    companion object {
        fun completelyRoundRectShape(context: Context): RoundRectShape {
            val outerRadii = FloatArray(8) {
                Screen.maxSide(context).toFloat()
            }
            return RoundRectShape(outerRadii, null, null)
        }
    }
}

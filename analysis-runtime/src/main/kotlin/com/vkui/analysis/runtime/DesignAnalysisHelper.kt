package com.vkui.analysis.runtime

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.children
import androidx.core.view.descendants
import com.google.common.collect.MapMaker
import com.vkui.analysis.runtime.shadow.DesignAnalysisLayoutParamsProperties
import com.vkui.analysis.runtime.shadow.DesignAnalysisTypedArrayProperties
import com.vkui.analysis.runtime.shadow.DesignAnalysisTypedValueProperties
import com.vkui.analysis.runtime.shadow.DesignAnalysisViewProperties
import java.util.concurrent.atomic.AtomicLong

@Suppress("unused")
object DesignAnalysisHelper {
    var anyViewTouchListener: ((View) -> Unit)? = null

    @JvmStatic
    val totalMethodCount = AtomicLong(0)

    @JvmStatic
    val taintedMethodCount = AtomicLong(0)

    @JvmStatic
    val shadowState = MapMaker().weakKeys().makeMap<Any, Any>()

    private var doNotDraw = false

    fun setAnyViewTouchListener(view: View, listener: (View) -> Unit) {
        anyViewTouchListener = listener
        redrawHierarchy(view)
    }

    fun clearAnyViewTouchListener(view: View) {
        anyViewTouchListener = null
        redrawHierarchy(view)
    }

    @JvmStatic
    fun getPropertiesOf(view: View): DesignAnalysisViewProperties? {
        return getShadowState<DesignAnalysisViewProperties>(view)
    }

    @JvmStatic
    fun getPropertiesOf(layoutParams: ViewGroup.LayoutParams): DesignAnalysisLayoutParamsProperties? {
        return getShadowState<DesignAnalysisLayoutParamsProperties>(layoutParams)
    }

    @JvmStatic
    fun incrementTotalMethodCount() {
        totalMethodCount.incrementAndGet()
    }

    @JvmStatic
    fun incrementTaintedMethodCount() {
        taintedMethodCount.incrementAndGet()
    }

    @JvmStatic
    fun setPadding(
        view: View,
        paddingLeft: Int,
        paddingTop: Int,
        paddingRight: Int,
        paddingBottom: Int
    ) {
        val properties = getShadowState(view) {
            DesignAnalysisViewProperties()
        }
        properties.paddingStart = DesignAnalysisTracker.getFromParameter(1)
        properties.paddingTop = DesignAnalysisTracker.getFromParameter(2)
        properties.paddingEnd = DesignAnalysisTracker.getFromParameter(3)
        properties.paddingBottom = DesignAnalysisTracker.getFromParameter(4)
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }

    @JvmStatic
    fun setPaddingRelative(
        view: View,
        paddingStart: Int,
        paddingTop: Int,
        paddingEnd: Int,
        paddingBottom: Int
    ) {
        val properties = getShadowState(view) {
            DesignAnalysisViewProperties()
        }
        properties.paddingStart = DesignAnalysisTracker.getFromParameter(1)
        properties.paddingTop = DesignAnalysisTracker.getFromParameter(2)
        properties.paddingEnd = DesignAnalysisTracker.getFromParameter(3)
        properties.paddingBottom = DesignAnalysisTracker.getFromParameter(4)
        view.setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
    }

    @JvmStatic
    fun getPaddingStart(view: View): Int {
        val properties = getShadowState<DesignAnalysisViewProperties>(view)
        DesignAnalysisTracker.setReturnValue(properties?.paddingStart)
        return view.paddingStart
    }

    @JvmStatic
    fun getPaddingTop(view: View): Int {
        val properties = getShadowState<DesignAnalysisViewProperties>(view)
        DesignAnalysisTracker.setReturnValue(properties?.paddingTop)
        return view.paddingTop
    }

    @JvmStatic
    fun getPaddingEnd(view: View): Int {
        val properties = getShadowState<DesignAnalysisViewProperties>(view)
        DesignAnalysisTracker.setReturnValue(properties?.paddingEnd)
        return view.paddingEnd
    }

    @JvmStatic
    fun getPaddingBottom(view: View): Int {
        val properties = getShadowState<DesignAnalysisViewProperties>(view)
        DesignAnalysisTracker.setReturnValue(properties?.paddingBottom)
        return view.paddingBottom
    }

    @JvmStatic
    fun getPaddingLeft(view: View): Int {
        val properties = getShadowState<DesignAnalysisViewProperties>(view)
        DesignAnalysisTracker.setReturnValue(properties?.paddingStart)
        return view.paddingLeft
    }

    @JvmStatic
    fun getPaddingRight(view: View): Int {
        val properties = getShadowState<DesignAnalysisViewProperties>(view)
        DesignAnalysisTracker.setReturnValue(properties?.paddingEnd)
        return view.paddingRight
    }

    @JvmStatic
    fun getLeftMargin(layoutParams: MarginLayoutParams): Int {
        val properties = getShadowState<DesignAnalysisLayoutParamsProperties>(layoutParams)
        DesignAnalysisTracker.setReturnValue(properties?.marginStart)
        return layoutParams.leftMargin
    }

    @JvmStatic
    fun getTopMargin(layoutParams: MarginLayoutParams): Int {
        val properties = getShadowState<DesignAnalysisLayoutParamsProperties>(layoutParams)
        DesignAnalysisTracker.setReturnValue(properties?.marginTop)
        return layoutParams.topMargin
    }

    @JvmStatic
    fun getRightMargin(layoutParams: MarginLayoutParams): Int {
        val properties = getShadowState<DesignAnalysisLayoutParamsProperties>(layoutParams)
        DesignAnalysisTracker.setReturnValue(properties?.marginEnd)
        return layoutParams.rightMargin
    }

    @JvmStatic
    fun getBottomMargin(layoutParams: MarginLayoutParams): Int {
        val properties = getShadowState<DesignAnalysisLayoutParamsProperties>(layoutParams)
        DesignAnalysisTracker.setReturnValue(properties?.marginBottom)
        return layoutParams.bottomMargin
    }

    @JvmStatic
    fun getMarginStart(layoutParams: MarginLayoutParams): Int {
        val properties = getShadowState<DesignAnalysisLayoutParamsProperties>(layoutParams)
        DesignAnalysisTracker.setReturnValue(properties?.marginStart)
        return layoutParams.marginStart
    }

    @JvmStatic
    fun getMarginEnd(layoutParams: MarginLayoutParams): Int {
        val properties = getShadowState<DesignAnalysisLayoutParamsProperties>(layoutParams)
        DesignAnalysisTracker.setReturnValue(properties?.marginEnd)
        return layoutParams.marginEnd
    }

    @JvmStatic
    fun setLeftMargin(layoutParams: MarginLayoutParams, margin: Int) {
        val properties = getShadowState(layoutParams) {
            DesignAnalysisLayoutParamsProperties()
        }
        properties.marginStart = DesignAnalysisTracker.getFromParameter(1)
        layoutParams.leftMargin = margin
    }

    @JvmStatic
    fun setMargins(layoutParams: MarginLayoutParams, left: Int, top: Int, right: Int, bottom: Int) {
        val properties = getShadowState(layoutParams) {
            DesignAnalysisLayoutParamsProperties()
        }
        properties.marginStart = DesignAnalysisTracker.getFromParameter(1)
        properties.marginTop = DesignAnalysisTracker.getFromParameter(2)
        properties.marginEnd = DesignAnalysisTracker.getFromParameter(3)
        properties.marginBottom = DesignAnalysisTracker.getFromParameter(4)
        layoutParams.setMargins(left, top, right, bottom)
    }

    @JvmStatic
    fun setTopMargin(layoutParams: MarginLayoutParams, margin: Int) {
        val properties = getShadowState(layoutParams) {
            DesignAnalysisLayoutParamsProperties()
        }
        properties.marginTop = DesignAnalysisTracker.getFromParameter(1)
        layoutParams.topMargin = margin
    }

    @JvmStatic
    fun setRightMargin(layoutParams: MarginLayoutParams, margin: Int) {
        val properties = getShadowState(layoutParams) {
            DesignAnalysisLayoutParamsProperties()
        }
        properties.marginEnd = DesignAnalysisTracker.getFromParameter(1)
        layoutParams.rightMargin = margin
    }

    @JvmStatic
    fun setBottomMargin(layoutParams: MarginLayoutParams, margin: Int) {
        val properties = getShadowState(layoutParams) {
            DesignAnalysisLayoutParamsProperties()
        }
        properties.marginBottom = DesignAnalysisTracker.getFromParameter(1)
        layoutParams.bottomMargin = margin
    }

    @JvmStatic
    fun setMarginStart(layoutParams: MarginLayoutParams, margin: Int) {
        val properties = getShadowState(layoutParams) {
            DesignAnalysisLayoutParamsProperties()
        }
        properties.marginStart = DesignAnalysisTracker.getFromParameter(1)
        layoutParams.marginStart = margin
    }

    @JvmStatic
    fun setMarginEnd(layoutParams: MarginLayoutParams, margin: Int) {
        val properties = getShadowState(layoutParams) {
            DesignAnalysisLayoutParamsProperties()
        }
        properties.marginEnd = DesignAnalysisTracker.getFromParameter(1)
        layoutParams.marginEnd = margin
    }

    @JvmStatic
    fun getData(typedValue: TypedValue): Int {
        val properties = getShadowState<DesignAnalysisTypedValueProperties>(typedValue)
        DesignAnalysisTracker.setReturnValue(properties?.dataState)
        return typedValue.data
    }

    @JvmStatic
    fun getFloat(typedValue: TypedValue): Float {
        val properties = getShadowState<DesignAnalysisTypedValueProperties>(typedValue)
        DesignAnalysisTracker.setReturnValue(properties?.dataState)
        return typedValue.float
    }

    @JvmStatic
    fun complexToDimension(data: Int, metrics: DisplayMetrics): Float {
        DesignAnalysisTracker.setReturnValue(DesignAnalysisTracker.getFromParameter(0))
        return TypedValue.complexToDimension(data, metrics)
    }

    @JvmStatic
    fun complexToDimensionPixelOffset(data: Int, metrics: DisplayMetrics): Int {
        DesignAnalysisTracker.setReturnValue(DesignAnalysisTracker.getFromParameter(0))
        return TypedValue.complexToDimensionPixelOffset(data, metrics)
    }

    @JvmStatic
    fun complexToDimensionPixelSize(data: Int, metrics: DisplayMetrics): Int {
        DesignAnalysisTracker.setReturnValue(DesignAnalysisTracker.getFromParameter(0))
        return TypedValue.complexToDimensionPixelOffset(data, metrics)
    }

    @JvmStatic
    fun applyDimension(unit: Int, value: Float, metrics: DisplayMetrics): Float {
        DesignAnalysisTracker.setReturnValue(DesignAnalysisTracker.getFromParameter(1))
        return TypedValue.applyDimension(unit, value, metrics)
    }

    @JvmStatic
    fun getDimensionPixelSize(typedArray: TypedArray, index: Int, defValue: Int): Int {
        getDimension(typedArray, index)
        return typedArray.getDimensionPixelSize(index, defValue)
    }

    @JvmStatic
    fun getDimensionPixelOffset(typedArray: TypedArray, index: Int, defValue: Int): Int {
        getDimension(typedArray, index)
        return typedArray.getDimensionPixelOffset(index, defValue)
    }

    @JvmStatic
    fun getDimension(typedArray: TypedArray, index: Int, defValue: Float): Float {
        getDimension(typedArray, index)
        return typedArray.getDimension(index, defValue)
    }

    @JvmStatic
    fun resolveAttribute(
        theme: Resources.Theme,
        attr: Int,
        outValue: TypedValue,
        resolveRefs: Boolean
    ): Boolean {
        val properties = getShadowState(outValue) {
            DesignAnalysisTypedValueProperties()
        }
        properties.dataState = DesignAnalysisTracker.getFromParameter(1)
        return theme.resolveAttribute(attr, outValue, resolveRefs)
    }

    @JvmStatic
    fun getDimension(
        resources: Resources,
        id: Int
    ): Float {
        DesignAnalysisTracker.setReturnValue(DesignAnalysisTracker.getFromParameter(1))
        return resources.getDimension(id)
    }

    @JvmStatic
    fun getDimensionPixelSize(
        resources: Resources,
        id: Int
    ): Int {
        DesignAnalysisTracker.setReturnValue(DesignAnalysisTracker.getFromParameter(1))
        return resources.getDimensionPixelSize(id)
    }

    @JvmStatic
    fun getDimensionPixelOffset(
        resources: Resources,
        id: Int
    ): Int {
        DesignAnalysisTracker.setReturnValue(DesignAnalysisTracker.getFromParameter(1))
        return resources.getDimensionPixelOffset(id)
    }

    @JvmStatic
    fun obtainStyledAttributes(
        context: Context,
        attributeSet: AttributeSet?,
        attrs: IntArray
    ): TypedArray {
        val delegate = context.obtainStyledAttributes(attributeSet, attrs)
        if (attributeSet != null) {
            shadowState[delegate] = DesignAnalysisTypedArrayProperties(context, attributeSet, attrs)
        }
        return delegate
    }

    @JvmStatic
    fun obtainStyledAttributes(
        context: Context,
        attributeSet: AttributeSet?,
        attrs: IntArray,
        defStyleAttr: Int,
        defStyleRes: Int
    ): TypedArray {
        val delegate = context.obtainStyledAttributes(
            attributeSet,
            attrs,
            defStyleAttr,
            defStyleRes
        )
        if (attributeSet != null) {
            shadowState[delegate] = DesignAnalysisTypedArrayProperties(context, attributeSet, attrs)
        }
        return delegate
    }

    @JvmStatic
    fun onTouchEvent(
        view: View,
        motionEvent: MotionEvent
    ): Boolean {
        val listener = anyViewTouchListener ?: return false
        listener(view)
        return true
    }

    @JvmStatic
    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    @JvmStatic
    private val textPaint = Paint().apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    @JvmStatic
    private val path = Path()

    @JvmStatic
    private val cutPath = Path()

    @JvmStatic
    fun onDrawForeground(view: View, canvas: Canvas) {
        if (doNotDraw) {
            return
        }
        val properties = getShadowState<DesignAnalysisViewProperties>(view)

        if (properties != null) {
            val paddingStart = getPropertyText(properties.paddingStart)
            val paddingTop = getPropertyText(properties.paddingTop)
            val paddingEnd = getPropertyText(properties.paddingEnd)
            val paddingBottom = getPropertyText(properties.paddingBottom)
            path.reset()
            cutPath.reset()
            path.addRect(0F, 0F, view.width.toFloat(), view.height.toFloat(), Path.Direction.CW)
            cutPath.addRect(
                view.paddingStart.toFloat(),
                view.paddingTop.toFloat(),
                (view.width - view.paddingEnd).toFloat(),
                (view.height - view.paddingBottom).toFloat(),
                Path.Direction.CW
            )
            path.op(cutPath, Path.Op.DIFFERENCE)
            canvas.drawPath(path, fillPaint.apply { color = 0x4090D5FF })

            textPaint.textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                6F,
                view.resources.displayMetrics
            )
            val textHeight = textPaint.fontMetrics.run { descent - ascent }
            textPaint.color = Color.BLACK
            if (view.paddingStart > 0) {
                canvas.rotate(-90F, view.paddingStart / 2F, view.height / 2F)
                canvas.drawText(
                    paddingStart,
                    view.paddingStart / 2F,
                    view.height / 2F + textHeight / 4F,
                    textPaint
                )
                canvas.rotate(90F, view.paddingStart / 2F, view.height / 2F)
            }
            if (view.paddingTop > 0) {
                canvas.drawText(
                    paddingTop,
                    view.width / 2F,
                    view.paddingTop / 2F + textHeight / 4F,
                    textPaint
                )
            }
            if (view.paddingEnd > 0) {
                canvas.rotate(-90F, (view.width - view.paddingEnd / 2F), view.height / 2F)
                canvas.drawText(
                    paddingEnd,
                    view.width - view.paddingEnd / 2F,
                    view.height / 2F + textHeight / 4F,
                    textPaint
                )
                canvas.rotate(90F, (view.width - view.paddingEnd / 2F), view.height / 2F)
            }
            if (view.paddingBottom > 0) {
                canvas.drawText(
                    paddingBottom,
                    view.width / 2F,
                    view.height - view.paddingBottom / 2F + textHeight / 4F,
                    textPaint
                )
            }
        }

        if (view is ViewGroup) {
            for (child in view.children) {
                val lp = child.layoutParams as? MarginLayoutParams ?: continue
                val properties = getShadowState<DesignAnalysisLayoutParamsProperties>(lp)
                    ?: continue
                val marginStart = getPropertyText(properties.marginStart)
                val marginTop = getPropertyText(properties.marginTop)
                val marginEnd = getPropertyText(properties.marginEnd)
                val marginBottom = getPropertyText(properties.marginBottom)
                path.reset()
                cutPath.reset()
                path.addRect(
                    (child.left - lp.leftMargin).toFloat(),
                    (child.top - lp.topMargin).toFloat(),
                    (child.right + lp.rightMargin).toFloat(),
                    (child.bottom + lp.bottomMargin).toFloat(),
                    Path.Direction.CW
                )
                cutPath.addRect(
                    child.left.toFloat(),
                    child.top.toFloat(),
                    child.right.toFloat(),
                    child.bottom.toFloat(),
                    Path.Direction.CW
                )
                path.op(cutPath, Path.Op.DIFFERENCE)
                canvas.drawPath(path, fillPaint.apply { color = 0x40FF00FF })

                textPaint.textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    6F,
                    view.resources.displayMetrics
                )
                val textHeight = textPaint.fontMetrics.run { descent - ascent }
                textPaint.color = Color.BLACK
                if (lp.leftMargin > 0) {
                    canvas.rotate(
                        -90F,
                        (child.left - lp.leftMargin / 2F),
                        child.top + child.height / 2F
                    )
                    canvas.drawText(
                        marginStart,
                        child.left - lp.leftMargin / 2F,
                        child.top + child.height / 2F + textHeight / 4F,
                        textPaint
                    )
                    canvas.rotate(
                        90F,
                        (child.left - lp.leftMargin / 2F),
                        child.top + child.height / 2F
                    )
                }
                if (lp.topMargin > 0) {
                    canvas.drawText(
                        marginTop,
                        child.left + child.width / 2F,
                        child.top - lp.topMargin / 2F + textHeight / 4F,
                        textPaint
                    )
                }
                if (lp.rightMargin > 0) {
                    canvas.rotate(
                        -90F,
                        (child.right + lp.rightMargin / 2F),
                        child.top + child.height / 2F
                    )
                    canvas.drawText(
                        marginEnd,
                        child.right + lp.rightMargin / 2F,
                        child.top + child.height / 2F + textHeight / 4F,
                        textPaint
                    )
                    canvas.rotate(
                        90F,
                        (child.right + lp.rightMargin / 2F),
                        child.top + child.height / 2F
                    )
                }
                if (lp.bottomMargin > 0) {
                    canvas.drawText(
                        marginBottom,
                        child.left + child.width / 2F,
                        child.bottom + lp.bottomMargin / 2F + textHeight / 4F,
                        textPaint
                    )
                }
            }
        }
    }

    @JvmStatic
    fun captureViewAttributes(
        view: View,
        attributeSet: AttributeSet?
    ) {
        attributeSet ?: return

        if (view is ViewGroup) {
            view.clipChildren = false
            view.clipToPadding = false
        }

        with(view.context) {
            val paddingStart = getDimensionAttributeMark(attributeSet, "paddingStart")
            val paddingTop = getDimensionAttributeMark(attributeSet, "paddingTop")
            val paddingEnd = getDimensionAttributeMark(attributeSet, "paddingEnd")
            val paddingBottom = getDimensionAttributeMark(attributeSet, "paddingBottom")
            val paddingLeft = getDimensionAttributeMark(attributeSet, "paddingLeft")
            val paddingRight = getDimensionAttributeMark(attributeSet, "paddingRight")
            val paddingHorizontal = getDimensionAttributeMark(attributeSet, "paddingHorizontal")
            val paddingVertical = getDimensionAttributeMark(attributeSet, "paddingVertical")
            val padding = getDimensionAttributeMark(attributeSet, "padding")

            val properties = getShadowState(view) { DesignAnalysisViewProperties() }
            properties.paddingStart = paddingStart ?: paddingLeft ?: paddingHorizontal ?: padding
            properties.paddingTop = paddingTop ?: paddingVertical ?: padding
            properties.paddingEnd = paddingEnd ?: paddingRight ?: paddingHorizontal ?: padding
            properties.paddingBottom = paddingBottom ?: paddingVertical ?: padding
        }
    }

    @JvmStatic
    fun captureLayoutParams(
        layoutParams: ViewGroup.LayoutParams,
        viewGroup: ViewGroup,
        attributeSet: AttributeSet,
    ) = with(viewGroup.context) {
        val marginStart = getDimensionAttributeMark(attributeSet, "layout_marginStart")
        val marginTop = getDimensionAttributeMark(attributeSet, "layout_marginTop")
        val marginEnd = getDimensionAttributeMark(attributeSet, "layout_marginEnd")
        val marginBottom = getDimensionAttributeMark(attributeSet, "layout_marginBottom")
        val marginLeft = getDimensionAttributeMark(attributeSet, "layout_marginLeft")
        val marginRight = getDimensionAttributeMark(attributeSet, "layout_marginRight")
        val marginHorizontal = getDimensionAttributeMark(attributeSet, "layout_marginHorizontal")
        val marginVertical = getDimensionAttributeMark(attributeSet, "layout_marginVertical")
        val margin = getDimensionAttributeMark(attributeSet, "layout_margin")

        val properties = getShadowState(layoutParams) {
            DesignAnalysisLayoutParamsProperties()
        }
        properties.marginStart = marginStart ?: marginLeft ?: marginHorizontal ?: margin
        properties.marginTop = marginTop ?: marginVertical ?: margin
        properties.marginEnd = marginEnd ?: marginRight ?: marginHorizontal ?: margin
        properties.marginBottom = marginBottom ?: marginVertical ?: margin
    }

    @JvmStatic
    fun captureLayoutParams(
        newLayoutParams: ViewGroup.LayoutParams,
        viewGroup: ViewGroup,
        oldLayoutParams: ViewGroup.LayoutParams,
    ) {
        val oldProperties = getShadowState<DesignAnalysisLayoutParamsProperties>(oldLayoutParams)
            ?: return
        shadowState[newLayoutParams] = oldProperties.copy()
    }

    private fun redrawHierarchy(view: View) {
        val rootView = view.rootView
        rootView.invalidate()
        (rootView as? ViewGroup)?.descendants?.forEach { it.invalidate() }
    }

    private fun getPropertyText(mark: DesignAnalysisMark?): String {
        val description = mark?.description
        return getPropertyText(description)
    }

    private fun getPropertyText(description: String?): String {
        return when {
            description == null -> "undefined"

            description.startsWith("literal") -> description.removePrefix("literal ")
                .removeSuffix("dip")

            description.startsWith("attribute") -> description.removePrefix("attribute ")
                .removePrefix("vk_ui_size_")
                .removePrefix("vk_ui_spacing_size")

            description.startsWith("resource") -> description.removePrefix("resource ")

            description.startsWith("negate") -> "(-${getPropertyText(description.removePrefix("negate "))})"

            else -> {
                val split = description.split(" ", limit = 2)
                if (split.size < 2) {
                    description
                } else {
                    val (op, rest) = split
                    val leftEnd = getParensEnd(rest)
                    val left = rest.substring(1, leftEnd)
                    val right = rest.substring(leftEnd + 3, rest.length - 1)
                    val leftValue = getPropertyText(left)
                    val rightValue = getPropertyText(right)
                    val sign = when (op) {
                        "add" -> "+"
                        "sub" -> "-"
                        "mul" -> "*"
                        "div" -> "/"
                        else -> op
                    }
                    "($leftValue $sign $rightValue)"
                }
            }
        }
    }

    private fun getParensEnd(text: String): Int {
        var counter = 0
        for (i in text.indices) {
            when (text[i]) {
                '(' -> counter++
                ')' -> counter--
            }
            if (counter == 0) {
                return i
            }
        }
        return text.length
    }

    private fun getDimension(typedArray: TypedArray, index: Int) {
        if (!typedArray.hasValue(index)) {
            DesignAnalysisTracker.setReturnValue(DesignAnalysisTracker.getFromParameter(2))
            return
        }

        val properties = getShadowState<DesignAnalysisTypedArrayProperties>(typedArray)
        val attributeIndex = properties?.let {
            val attrId = properties.attrs[index]
            (0 until properties.attributeSet.attributeCount).firstOrNull { i ->
                properties.attributeSet.getAttributeNameResource(i) == attrId
            }
        }
        if (attributeIndex == null) {
            DesignAnalysisTracker.setReturnValue(DesignAnalysisTracker.getFromParameter(2))
            return
        }

        val value = properties.attributeSet.getAttributeValue(attributeIndex)
        val mark = with(properties.context) {
            getDimensionValueMark(value)
        }
        DesignAnalysisTracker.setReturnValue(mark)
    }

    private inline fun <reified T> getShadowState(obj: Any?): T? {
        return shadowState.compute(obj) { _, existing ->
            existing as? T
        } as? T
    }

    private inline fun <reified T> getShadowState(obj: Any?, crossinline default: () -> T): T {
        return shadowState.compute(obj) { _, existing ->
            (existing as? T) ?: default()
        } as T
    }

    private fun Context.getDimensionAttributeMark(
        attributeSet: AttributeSet,
        name: String
    ): DesignAnalysisMark? {
        val namespace = "http://schemas.android.com/apk/res/android"
        val actualValue = attributeSet.getAttributeValue(namespace, name)
        return getDimensionValueMark(actualValue)
    }

    private fun Context.getDimensionValueMark(actualValue: String?): DesignAnalysisMark? {
        return when {
            actualValue == null -> null
            actualValue.startsWith("?") -> {
                val resourceId = actualValue.substring(1).toIntOrNull()
                val mark = resourceId?.let {
                    resources.getResourceEntryName(resourceId)
                } ?: actualValue
                DesignAnalysisMark("attribute $mark")
            }

            actualValue.startsWith("@") -> {
                val resourceId = actualValue.substring(1).toIntOrNull()
                val mark = resourceId?.let {
                    resources.getResourceEntryName(resourceId)
                } ?: actualValue
                DesignAnalysisMark("resource $mark")
            }

            else -> DesignAnalysisMark("literal $actualValue")
        }
    }
}

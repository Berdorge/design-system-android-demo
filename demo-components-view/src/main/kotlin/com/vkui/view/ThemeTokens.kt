package com.vkui.view

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat

fun Context.resolveDimension(@AttrRes attrRes: Int) = resolve(attrRes, 0F) {
    TypedValue.complexToDimension(data, resources.displayMetrics)
}

fun Context.resolveDimensionPixelSize(@AttrRes attrRes: Int) = resolve(attrRes, 0) {
    TypedValue.complexToDimensionPixelSize(data, resources.displayMetrics)
}

fun Context.resolveDimensionPixelOffset(@AttrRes attrRes: Int) = resolve(attrRes, 0) {
    TypedValue.complexToDimensionPixelOffset(data, resources.displayMetrics)
}

fun Context.resolveColor(@AttrRes attrRes: Int) = resolve(attrRes, 0) {
    data
}

fun Context.resolveFloat(@AttrRes attrRes: Int) = resolve(attrRes, 0F) {
    float
}

fun Context.resolveDrawable(@AttrRes attrRes: Int) = resolve(attrRes, null) {
    ContextCompat.getDrawable(this@resolveDrawable, resourceId)
}

fun Context.resolveTextAppearance(@AttrRes attrRes: Int) = resolve(attrRes, 0) {
    resourceId
}

private inline fun <T> Context.resolve(
    @AttrRes attrRes: Int,
    default: T,
    resolution: TypedValue.() -> T
): T {
    val typedValue = TypedValue()
    return if (theme.resolveAttribute(attrRes, typedValue, true)) {
        typedValue.resolution()
    } else {
        default
    }
}

package com.vkui.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.core.content.res.use

fun Context.resolveDimension(@AttrRes attrRes: Int) = resolve(attrRes) {
    getDimension(0, 0F)
}

fun Context.resolveDimensionPixelSize(@AttrRes attrRes: Int) = resolve(attrRes) {
    getDimensionPixelSize(0, 0)
}

fun Context.resolveColor(@AttrRes attrRes: Int) = resolve(attrRes) {
    getColor(0, Color.BLACK)
}

fun Context.resolveFloat(@AttrRes attrRes: Int) = resolve(attrRes) {
    getFloat(0, 0F)
}

private inline fun <T> Context.resolve(
    @AttrRes attrRes: Int,
    resolution: TypedArray.() -> T
): T {
    val attrs = intArrayOf(attrRes)
    return obtainStyledAttributes(attrs).use { typedArray ->
        typedArray.resolution()
    }
}

package com.vkui.view

import android.content.Context
import kotlin.math.max

object Screen {
    fun width(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun height(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    fun maxSide(context: Context): Int {
        return max(width(context), height(context))
    }

    fun Int.dpToPx(context: Context): Int {
        return (this * context.density()).toInt()
    }

    fun Float.dpToPx(context: Context): Float {
        return this * context.density()
    }

    private fun Context.density() = resources.displayMetrics.density
}

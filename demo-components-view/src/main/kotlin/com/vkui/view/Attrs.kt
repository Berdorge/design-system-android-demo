package com.vkui.view

import android.content.res.TypedArray

inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, defaultValue: T): T {
    val ordinal = getInt(index, defaultValue.ordinal)
    return enumValues<T>().getOrElse(ordinal) { defaultValue }
}

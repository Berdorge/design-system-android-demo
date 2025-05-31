package com.vkui.components

import androidx.compose.ui.Modifier

inline fun Modifier.thenIf(condition: Boolean, block: () -> Modifier) = if (condition) {
    this then block()
} else {
    this
}

inline fun <T : Any> Modifier.thenIfNotNull(value: T?, block: (T) -> Modifier) = if (value != null) {
    this then block(value)
} else {
    this
}

package com.vkui.components

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@JvmInline
@Immutable
value class ImmutableMap<K, V>(val map: Map<K, V>) : Map<K, V> by map

@JvmInline
@Immutable
value class ImmutableList<T>(val list: List<T>) : List<T> by list

@Stable
fun <K, V> Map<K, V>.makeImmutable(): ImmutableMap<K, V> = ImmutableMap(this)

@Stable
fun <T> List<T>.makeImmutable(): ImmutableList<T> = ImmutableList(this)

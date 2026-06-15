package com.vk.tools.designanalysis.hierarchy

import kotlinx.serialization.Serializable

@Serializable
data class DeclaredField(
    val className: String,
    val name: String,
    val descriptor: String,
) {
    override fun toString(): String = "$descriptor $className.$name"
}

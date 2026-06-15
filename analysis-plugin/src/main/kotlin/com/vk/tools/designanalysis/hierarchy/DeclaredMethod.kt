package com.vk.tools.designanalysis.hierarchy

import kotlinx.serialization.Serializable

@Serializable
data class DeclaredMethod(
    val className: String,
    val nameWithArgumentsDescriptor: String,
    val returnTypeDescriptor: String,
) {
    override fun toString(): String = "$className.$nameWithArgumentsDescriptor$returnTypeDescriptor"

    fun getName() = nameWithArgumentsDescriptor.substringBefore('(')

    fun getDescriptor() = "(${nameWithArgumentsDescriptor.substringAfter('(')}$returnTypeDescriptor"

    companion object {
        fun make(className: String, name: String, descriptor: String): DeclaredMethod {
            val argumentsEnd = descriptor.indexOf(')')
            return DeclaredMethod(
                className,
                name + descriptor.substring(0, argumentsEnd + 1),
                descriptor.substring(argumentsEnd + 1)
            )
        }
    }
}

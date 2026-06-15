package com.vk.tools.designanalysis.taint

import com.vk.tools.designanalysis.hierarchy.DeclaredField
import com.vk.tools.designanalysis.hierarchy.DeclaredMethod
import kotlinx.serialization.Serializable

@Serializable
sealed class PossiblyTaintedSource {
    @Serializable
    data class MethodArgument(
        val method: DeclaredMethod,

        /**
         * Does not include "this" parameter ever.
         * The domain of the task does not include objects,
         * only primitives
         */
        val index: Int
    ) : PossiblyTaintedSource()

    @Serializable
    data class MethodReturn(
        val method: DeclaredMethod
    ) : PossiblyTaintedSource()

    @Serializable
    data class Field(
        val field: DeclaredField
    ) : PossiblyTaintedSource()
}

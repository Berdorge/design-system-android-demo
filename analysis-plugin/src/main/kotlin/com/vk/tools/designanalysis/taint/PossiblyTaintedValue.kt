package com.vk.tools.designanalysis.taint

import org.objectweb.asm.tree.analysis.Value

data class PossiblyTaintedValue(
    val valueSize: Int,
    val sources: Set<PossiblyTaintedSource>
) : Value {
    override fun getSize(): Int = valueSize
}

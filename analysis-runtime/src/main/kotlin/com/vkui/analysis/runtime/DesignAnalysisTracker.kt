package com.vkui.analysis.runtime

internal object DesignAnalysisTracker {
    private val args = ThreadLocal.withInitial {
        arrayOfNulls<DesignAnalysisMark?>(256)
    }
    private val returnValue = ThreadLocal<DesignAnalysisMark?>()

    @JvmStatic
    fun setReturnValue(value: DesignAnalysisMark?) {
        returnValue.set(value)
    }

    @JvmStatic
    fun getReturnValue(): DesignAnalysisMark? {
        return returnValue.get()
    }

    @JvmStatic
    fun getFromParameter(parameter: Int): DesignAnalysisMark? {
        return args.get()[parameter]
    }

    @JvmStatic
    fun setParameter(parameterIndex: Int, value: DesignAnalysisMark?) {
        val argValues = args.get()
        argValues[parameterIndex] = value
    }

    @JvmStatic
    fun negate(mark: DesignAnalysisMark?): DesignAnalysisMark? {
        return mark?.let {
            DesignAnalysisMark("negate ${mark.description}")
        }
    }

    @JvmStatic
    fun binaryOperation(
        operation: String,
        left: DesignAnalysisMark?,
        right: DesignAnalysisMark?
    ): DesignAnalysisMark? {
        if (left == null && right == null) {
            return null
        }
        return DesignAnalysisMark("$operation (${left?.description}) (${right?.description})")
    }
}

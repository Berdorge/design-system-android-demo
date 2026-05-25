package com.vkui.view.components

object Example {
    @JvmStatic
    fun example(a: Boolean, b: Boolean, c: Boolean) {
        val intermediate = if (a) {
            produce1()
        } else {
            produce2()
        }
        if (b) {
            consume1(intermediate)
        } else {
            consume2(intermediate)
        }
        if (c) {
            consume3(intermediate)
        } else {
            consume4(intermediate)
        }
    }

    @JvmStatic
    private fun produce1() = 1

    @JvmStatic
    private fun produce2() = 2

    @JvmStatic
    private fun consume1(value: Int) = Unit

    @JvmStatic
    private fun consume2(value: Int) = Unit

    @JvmStatic
    private fun consume3(value: Int) = Unit

    @JvmStatic
    private fun consume4(value: Int) = Unit
}

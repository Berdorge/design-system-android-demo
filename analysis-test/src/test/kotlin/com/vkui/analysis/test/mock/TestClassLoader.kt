package com.vkui.analysis.test.mock

class TestClassLoader(
    parent: ClassLoader = Thread.currentThread().contextClassLoader,
) : ClassLoader(parent) {
    fun defineClass(name: String, bytes: ByteArray): Class<*> {
        return defineClass(name, bytes, 0, bytes.size)
    }
}

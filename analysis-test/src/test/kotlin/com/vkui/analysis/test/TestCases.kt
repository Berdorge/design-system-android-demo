package com.vkui.analysis.test

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import cases.DoubleArgAttribute
import cases.DoubleArgLiteral
import cases.DoubleFieldAttribute
import cases.DoubleFieldLiteral
import cases.DoubleReturnAttribute
import cases.DoubleReturnLiteral
import cases.FloatArgAttribute
import cases.FloatArgLiteral
import cases.FloatFieldAttribute
import cases.FloatFieldLiteral
import cases.FloatReturnAttribute
import cases.FloatReturnLiteral
import cases.IntArgAttribute
import cases.IntArgLiteral
import cases.IntFieldAttribute
import cases.IntFieldLiteral
import cases.IntReturnAttribute
import cases.IntReturnLiteral
import cases.XmlAttribute
import cases.XmlLiteral
import cases.XmlResource
import com.vk.tools.designanalysis.ClassVisitorChain
import com.vk.tools.designanalysis.instrumentation.ConcreteViewsVisitor
import com.vk.tools.designanalysis.instrumentation.CreateApplicationFieldMarksVisitor
import com.vk.tools.designanalysis.instrumentation.ModelledReplacementVisitor
import com.vk.tools.designanalysis.instrumentation.PrimitivesDataflowAnalysis
import com.vkui.analysis.runtime.DesignAnalysisLayoutInflaterFactory
import com.vkui.analysis.runtime.view.DesignAnalysisFrameLayout
import com.vkui.analysis.runtime.view.DesignAnalysisTextView
import com.vkui.analysis.test.mock.TestActivity
import com.vkui.analysis.test.mock.TestAnalysisValidator
import com.vkui.analysis.test.mock.TestClassContext
import com.vkui.analysis.test.mock.TestClassLoader
import org.junit.Test
import org.junit.runner.RunWith
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class TestCases {
    @Test
    fun testIntReturnLiteral() {
        testCases(IntReturnLiteral::class.java)
    }

    @Test
    fun testIntReturnAttribute() {
        testCases(IntReturnAttribute::class.java)
    }

    @Test
    fun testIntArgLiteral() {
        testCases(IntArgLiteral::class.java)
    }

    @Test
    fun testIntArgAttribute() {
        testCases(IntArgAttribute::class.java)
    }

    @Test
    fun testIntFieldLiteral() {
        testCases(IntFieldLiteral::class.java)
    }

    @Test
    fun testIntFieldAttribute() {
        testCases(IntFieldAttribute::class.java)
    }

    @Test
    fun testFloatReturnLiteral() {
        testCases(FloatReturnLiteral::class.java)
    }

    @Test
    fun testFloatReturnAttribute() {
        testCases(FloatReturnAttribute::class.java)
    }

    @Test
    fun testFloatArgLiteral() {
        testCases(FloatArgLiteral::class.java)
    }

    @Test
    fun testFloatArgAttribute() {
        testCases(FloatArgAttribute::class.java)
    }

    @Test
    fun testFloatFieldLiteral() {
        testCases(FloatFieldLiteral::class.java)
    }

    @Test
    fun testFloatFieldAttribute() {
        testCases(FloatFieldAttribute::class.java)
    }

    @Test
    fun testDoubleReturnLiteral() {
        testCases(DoubleReturnLiteral::class.java)
    }

    @Test
    fun testDoubleReturnAttribute() {
        testCases(DoubleReturnAttribute::class.java)
    }

    @Test
    fun testDoubleArgLiteral() {
        testCases(DoubleArgLiteral::class.java)
    }

    @Test
    fun testDoubleArgAttribute() {
        testCases(DoubleArgAttribute::class.java)
    }

    @Test
    fun testDoubleFieldLiteral() {
        testCases(DoubleFieldLiteral::class.java)
    }

    @Test
    fun testDoubleFieldAttribute() {
        testCases(DoubleFieldAttribute::class.java)
    }

    @Test
    fun testXmlLiteral() {
        testCases(XmlLiteral::class.java)
    }

    @Test
    fun testXmlResource() {
        testCases(XmlResource::class.java)
    }

    @Test
    fun testXmlAttribute() {
        testCases(XmlAttribute::class.java)
    }

    private fun testCases(vararg cases: Class<*>) {
        val testClassLoader = TestClassLoader()
        for (clazz in baseClasses) {
            instrumentClass(testClassLoader, clazz)
        }

        for (case in cases) {
            instrumentClass(testClassLoader, case)
        }

        Robolectric.buildActivity(
            testClassLoader.loadClass(TestActivity::class.java.name) as Class<Activity>
        ).use { controller ->
            controller.setup()
            val activity = controller.get()
            for (case in cases) {
                val instrumentedClass = testClassLoader.loadClass(case.name)
                val instance = instrumentedClass.getConstructor().newInstance()
                val method = instrumentedClass.getMethod("test", Context::class.java)
                method.invoke(instance, activity)
            }
        }
    }

    private fun instrumentClass(classLoader: TestClassLoader, clazz: Class<*>) {
        val classReader = ClassReader(clazz.name)
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val validator = TestAnalysisValidator()
        val classContext = TestClassContext(classLoader, clazz)
        val classVisitor = ClassVisitorChain
            .then { delegate -> CreateApplicationFieldMarksVisitor(delegate) }
            .then { delegate -> ModelledReplacementVisitor(delegate, classContext) }
            .then { delegate -> ConcreteViewsVisitor(delegate, classContext) }
            .then { delegate ->
                PrimitivesDataflowAnalysis(
                    delegate = delegate,
                    validator = validator,
                    isTainted = { true },
                    shouldInstrumentMethod = { true },
                )
            }
            .finally(classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        classLoader.defineClass(clazz.name, classWriter.toByteArray())
    }

    companion object {
        private val baseClasses = listOf(
            DesignAnalysisFrameLayout::class.java,
            DesignAnalysisLayoutInflaterFactory::class.java,
            DesignAnalysisTextView::class.java,
            R.attr::class.java,
            R.dimen::class.java,
            R.layout::class.java,
            R.style::class.java,
            TestActivity::class.java,
        )
    }
}

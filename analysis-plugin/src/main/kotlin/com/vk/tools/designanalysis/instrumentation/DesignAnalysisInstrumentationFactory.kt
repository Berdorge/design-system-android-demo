package com.vk.tools.designanalysis.instrumentation

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.vk.tools.designanalysis.ClassVisitorChain
import org.objectweb.asm.ClassVisitor

abstract class DesignAnalysisInstrumentationFactory : AsmClassVisitorFactory<InstrumentationParameters> {
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return ClassVisitorChain.Companion
            .then { delegate -> CreateApplicationFieldMarksVisitor(delegate) }
            .then { delegate -> ModelledReplacementVisitor(delegate, classContext) }
            .then { delegate -> ConcreteViewsVisitor(delegate, classContext) }
            .finally(nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return true
    }
}

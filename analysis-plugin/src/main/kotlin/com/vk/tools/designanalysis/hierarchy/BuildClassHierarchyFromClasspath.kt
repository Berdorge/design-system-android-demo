package com.vk.tools.designanalysis.hierarchy

import com.vk.tools.designanalysis.forEachClassInDir
import com.vk.tools.designanalysis.forEachClassInJar
import com.vk.tools.designanalysis.instrumentation.AnalysisValidator
import com.vk.tools.designanalysis.instrumentation.ClassSetAnalysisValidator
import org.objectweb.asm.ClassReader
import java.io.File
import java.io.InputStream

fun buildClassHierarchyFromClasspath(
    inputJars: List<File>,
    inputDirectories: List<File>,
    bootClasspath: List<File>,
): ClassHierarchyResult {
    val classHierarchy = SimpleClassHierarchy()
    val fillHierarchy = { inputStream: InputStream ->
        ClassReader(inputStream).accept(
            BuildHierarchyVisitor(classHierarchy),
            ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
        )
    }
    for (jar in inputJars) {
        forEachClassInJar(jar, fillHierarchy)
    }
    for (dir in inputDirectories) {
        forEachClassInDir(dir, fillHierarchy)
    }
    val applicationSuperclassOf = classHierarchy.getAllSuperclassRelations().toMap()
    val applicationFields = classHierarchy.getAllDeclaredFields().toMap()
    val analysisValidator = ClassSetAnalysisValidator(
        applicationSuperclassOf,
        applicationFields
    )
    for (jar in bootClasspath) {
        forEachClassInJar(jar, fillHierarchy)
    }
    return ClassHierarchyResult(classHierarchy, analysisValidator)
}

data class ClassHierarchyResult(
    val classHierarchy: ClassHierarchy,
    val analysisValidator: AnalysisValidator,
)

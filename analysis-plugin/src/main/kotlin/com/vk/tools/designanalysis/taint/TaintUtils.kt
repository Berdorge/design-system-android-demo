package com.vk.tools.designanalysis.taint

import com.vk.tools.designanalysis.RUNTIME_HELPER_NAME
import com.vk.tools.designanalysis.hierarchy.ClassHierarchy
import com.vk.tools.designanalysis.hierarchy.DeclaredField
import com.vk.tools.designanalysis.hierarchy.DeclaredMethod
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.Field
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource.MethodArgument
import kotlinx.serialization.json.Json
import org.objectweb.asm.Type
import java.io.File

fun findAllTainted(
    taintEdgesDirectory: File,
    classHierarchy: ClassHierarchy,
): Set<PossiblyTaintedSource> {
    val allTaintSources = mutableMapOf<PossiblyTaintedSource, MutableSet<PossiblyTaintedSource>>()
    taintEdgesDirectory.walkTopDown()
        .filter { it.isFile && it.extension == "txt" }
        .forEach { file ->
            file.bufferedReader().useLines { lines ->
                for (line in lines) {
                    val edge: Pair<PossiblyTaintedSource, PossiblyTaintedSource> =
                        Json.decodeFromString(line)
                    allTaintSources.computeIfAbsent(edge.first) { mutableSetOf() }
                        .add(edge.second)
                }
            }
        }
    return findAllTainted(tainted = allTaintSources, classHierarchy = classHierarchy)
}

fun findAllTainted(
    tainted: Map<PossiblyTaintedSource, Set<PossiblyTaintedSource>>,
    classHierarchy: ClassHierarchy,
): Set<PossiblyTaintedSource> {
    val thisStageTainted = getInitialTaints(classHierarchy)
    val allTainted = HashSet(thisStageTainted)

    while (thisStageTainted.isNotEmpty()) {
        val previousStageTainted = thisStageTainted.toList()
        thisStageTainted.clear()
        for (taint in previousStageTainted) {
            tainted[taint]?.forEach { newTaint ->
                if (allTainted.add(newTaint)) {
                    thisStageTainted.add(newTaint)
                }
            }
        }
    }

    return allTainted
}

fun getInitialTaints(classHierarchy: ClassHierarchy): HashSet<PossiblyTaintedSource> {
    val initiallyTainted = listOf(
        MethodArgument(DeclaredMethod.make("android/view/View", "setPadding", "(IIII)V"), 0),
        MethodArgument(DeclaredMethod.make("android/view/View", "setPadding", "(IIII)V"), 1),
        MethodArgument(DeclaredMethod.make("android/view/View", "setPadding", "(IIII)V"), 2),
        MethodArgument(DeclaredMethod.make("android/view/View", "setPadding", "(IIII)V"), 3),
        MethodArgument(
            DeclaredMethod.make(
                "android/view/View",
                "setPaddingRelative",
                "(IIII)V"
            ), 0
        ),
        MethodArgument(
            DeclaredMethod.make(
                "android/view/View",
                "setPaddingRelative",
                "(IIII)V"
            ), 1
        ),
        MethodArgument(
            DeclaredMethod.make(
                "android/view/View",
                "setPaddingRelative",
                "(IIII)V"
            ), 2
        ),
        MethodArgument(
            DeclaredMethod.make(
                "android/view/View",
                "setPaddingRelative",
                "(IIII)V"
            ), 3
        ),
        MethodArgument(
            DeclaredMethod.make(
                "android/view/ViewGroup\$MarginLayoutParams",
                "setMargins",
                "(IIII)V"
            ), 0
        ),
        MethodArgument(
            DeclaredMethod.make(
                "android/view/ViewGroup\$MarginLayoutParams",
                "setMargins",
                "(IIII)V"
            ), 1
        ),
        MethodArgument(
            DeclaredMethod.make(
                "android/view/ViewGroup\$MarginLayoutParams",
                "setMargins",
                "(IIII)V"
            ), 2
        ),
        MethodArgument(
            DeclaredMethod.make(
                "android/view/ViewGroup\$MarginLayoutParams",
                "setMargins",
                "(IIII)V"
            ), 3
        ),
        MethodArgument(
            DeclaredMethod.make(
                "android/view/ViewGroup\$MarginLayoutParams",
                "setMarginEnd",
                "(I)V"
            ), 0
        ),
        MethodArgument(
            DeclaredMethod.make(
                "android/view/ViewGroup\$MarginLayoutParams",
                "setMarginStart",
                "(I)V"
            ), 0
        ),
        Field(DeclaredField("android/view/ViewGroup\$MarginLayoutParams", "leftMargin", "I")),
        Field(DeclaredField("android/view/ViewGroup\$MarginLayoutParams", "topMargin", "I")),
        Field(DeclaredField("android/view/ViewGroup\$MarginLayoutParams", "rightMargin", "I")),
        Field(DeclaredField("android/view/ViewGroup\$MarginLayoutParams", "bottomMargin", "I")),
    )
    val result = hashSetOf<PossiblyTaintedSource>()
    for (source in initiallyTainted) {
        when (source) {
            is Field -> {
                for (target in classHierarchy.getPossibleTargets(source.field)) {
                    result.add(Field(target))
                }
            }

            is MethodArgument -> {
                for (target in classHierarchy.getPossibleTargets(source.method)) {
                    result.add(MethodArgument(target, source.index))
                }
            }

            is PossiblyTaintedSource.MethodReturn -> result.add(source)
        }
    }
    for (method in classHierarchy.getDeclaredMethods(RUNTIME_HELPER_NAME)) {
        for (i in 0 until Type.getArgumentCount(method.getDescriptor())) {
            result.add(MethodArgument(method, i))
        }
    }
    return result
}

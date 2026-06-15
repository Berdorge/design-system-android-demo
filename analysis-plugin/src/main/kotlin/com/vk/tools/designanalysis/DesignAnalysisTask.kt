package com.vk.tools.designanalysis

import com.vk.tools.designanalysis.hierarchy.ClassHierarchy
import com.vk.tools.designanalysis.hierarchy.DeclaredMethod
import com.vk.tools.designanalysis.hierarchy.buildClassHierarchyFromClasspath
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource
import com.vk.tools.designanalysis.taint.TaintVisitor
import com.vk.tools.designanalysis.taint.findAllTainted
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.objectweb.asm.ClassReader
import java.io.File
import java.io.InputStream
import java.io.ObjectInputStream
import kotlin.io.path.Path

abstract class DesignAnalysisTask : DefaultTask() {
    @get:InputFiles
    @get:Incremental
    abstract val inputJars: ConfigurableFileCollection

    @get:InputFiles
    @get:Incremental
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectories: ConfigurableFileCollection

//    @get:InputFile
//    @get:Incremental
//    abstract val fileToProjectName: RegularFileProperty

    @get:Internal
    abstract val fileToProjectName: SetProperty<ResolvedArtifactResult>

    @get:InputFiles
    abstract val bootClasspath: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val taintEdgesDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val changedMethodsDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val intoJarDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val intoDirDirectory: DirectoryProperty

    @TaskAction
    fun execute(changes: InputChanges) {
        val jarChanges = changes.getFileChanges(inputJars)
        val dirChanges = changes.getFileChanges(inputDirectories)

        val inputJars = inputJars.toList()
        val inputDirectories = inputDirectories.toList()
        val bootClasspath = bootClasspath.toList()

        val intoJarDirectory = intoJarDirectory.get().asFile
        val intoDirDirectory = intoDirDirectory.get().asFile
        val taintEdgesDirectory = taintEdgesDirectory.get().asFile
        val changedMethodsDirectory = changedMethodsDirectory.get().asFile
        val fileToProjectName =
            fileToProjectName.get().associate {
                it.file.absolutePath to it.id.componentIdentifier.displayName
            }
//            buildMap {
//            fileToProjectName.asFile.get().bufferedReader(Charsets.UTF_8).useLines { lines ->
//                for (line in lines) {
//                    val (filePath, projectName) = line.split("=", limit = 2)
//                    put(filePath, projectName)
//                }
//            }
//        }

        if (!changes.isIncremental) {
            intoJarDirectory.deleteRecursively()
            intoDirDirectory.deleteRecursively()
            taintEdgesDirectory.deleteRecursively()
            changedMethodsDirectory.deleteRecursively()
            intoJarDirectory.mkdirs()
            intoDirDirectory.mkdirs()
            taintEdgesDirectory.mkdirs()
            changedMethodsDirectory.mkdirs()
        }

        val (classHierarchy, _) = buildClassHierarchyFromClasspath(
            inputJars,
            inputDirectories,
            bootClasspath
        )

        val allOutputJars = intoJarDirectory.listFiles().mapTo(mutableSetOf()) { jar ->
            jar.name
        }
        for (jar in inputJars.minus(jarChanges.filter { it.changeType == ChangeType.ADDED }
            .mapTo(mutableSetOf()) { it.file })) {
            val filename = fileToProjectName[jar.absolutePath]?.plus(".jar") ?: jar.name
            allOutputJars.remove(filename)
        }
        for (removedJar in allOutputJars) {
            File(intoJarDirectory, removedJar).delete()
            File(taintEdgesDirectory, "$removedJar.txt").delete()
            File(changedMethodsDirectory, "$removedJar.txt").delete()
        }

        for (change in dirChanges) {
            if (change.changeType == ChangeType.REMOVED) {
                val changeFilePath = change.file.toPath()
                val dir = changeFilePath.subpath(
                    0,
                    changeFilePath.nameCount - Path(change.normalizedPath).nameCount
                ).toFile()
                val filename = "${dir.absolutePathId()}-${dir.name}"
                val intoDir = intoDirDirectory.resolve(filename)
                val taintEdgesTxt = taintEdgesDirectory.resolve("$filename.txt")
                val changedMethodsTxt = changedMethodsDirectory.resolve("$filename.txt")
                intoDir.deleteRecursively()
                taintEdgesTxt.delete()
                changedMethodsTxt.delete()
            }
        }

        for (jar in inputJars) {
            val isJarChanged = jarChanges.any { change -> change.file == jar }
            if (!isJarChanged) {
                continue
            }

            val filename = fileToProjectName[jar.absolutePath]?.plus(".jar") ?: jar.name
            val taintEdgesTxt = taintEdgesDirectory.resolve("$filename.txt")
            jar.copyTo(intoJarDirectory.resolve(filename), overwrite = true)
            taintEdgesTxt
                .outputStream()
                .bufferedWriter()
                .use { edgesOutput ->
                    forEachClassInJar(jar) { inputStream ->
                        val edges = findTaintEdges(inputStream, classHierarchy)
                        for (edge in edges) {
                            edgesOutput.write(Json.encodeToString(edge))
                            edgesOutput.newLine()
                        }
                    }
                }
        }

        for (dir in inputDirectories) {
            val isDirChanged = dirChanges.any { change ->
                change.file.relativeToOrNull(dir) != null
            }
            if (!isDirChanged) {
                continue
            }

            val filename = "${dir.absolutePathId()}-${dir.name}"
            val taintEdgesTxt = taintEdgesDirectory.resolve("$filename.txt")
            dir.copyRecursively(intoDirDirectory.resolve(filename), overwrite = true)
            taintEdgesTxt
                .outputStream()
                .bufferedWriter()
                .use { edgesOutput ->
                    forEachClassInDir(dir) { inputStream ->
                        val taintEdges = findTaintEdges(inputStream, classHierarchy)
                        for (edge in taintEdges) {
                            edgesOutput.write(Json.encodeToString(edge))
                            edgesOutput.newLine()
                        }
                    }
                }
        }

        val allTainted = findAllTainted(taintEdgesDirectory, classHierarchy)
        for (jar in inputJars) {
            val filename = fileToProjectName[jar.absolutePath]?.plus(".jar") ?: jar.name
            val changedMethodsTxt = changedMethodsDirectory.resolve("$filename.txt")
            val changedMethods = mutableSetOf<DeclaredMethod>()
            changedMethodsTxt
                .outputStream()
                .bufferedWriter()
                .use { output ->
                    val action = { inputStream: InputStream ->
                        val visitor = ChangedMethodsVisitor(allTainted, changedMethods)
                        ClassReader(inputStream).accept(
                            visitor,
                            ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
                        )
                    }
                    forEachClassInJar(jar, action)
                    for (changedMethod in changedMethods) {
                        output.write(Json.encodeToString(changedMethod))
                        output.newLine()
                    }
                }
        }
        for (dir in inputDirectories) {
            val filename = "${dir.absolutePathId()}-${dir.name}"
            val changedMethodsTxt = changedMethodsDirectory.resolve("$filename.txt")
            val changedMethods = mutableSetOf<DeclaredMethod>()
            changedMethodsTxt
                .outputStream()
                .bufferedWriter()
                .use { output ->
                    val action = { inputStream: InputStream ->
                        val visitor = ChangedMethodsVisitor(allTainted, changedMethods)
                        ClassReader(inputStream).accept(
                            visitor,
                            ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
                        )
                    }
                    forEachClassInDir(dir, action)
                    for (changedMethod in changedMethods) {
                        output.write(Json.encodeToString(changedMethod))
                        output.newLine()
                    }
                }
        }
    }

    private fun findTaintEdges(
        inputStream: InputStream,
        classHierarchy: ClassHierarchy,
    ): List<Pair<PossiblyTaintedSource, PossiblyTaintedSource>> {
        val classReader = ClassReader(inputStream)
        val taintEdges = mutableMapOf<PossiblyTaintedSource, MutableSet<PossiblyTaintedSource>>()
        val taintVisitor = TaintVisitor(taintEdges, classHierarchy)
        classReader.accept(taintVisitor, 0)
        return taintEdges.flatMapTo(mutableSetOf()) { (source, sinks) ->
            sinks.map { sink -> source to sink }
        }.toList()
    }
}

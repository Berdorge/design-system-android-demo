package com.vk.tools.designanalysis

import com.vk.tools.designanalysis.hierarchy.DeclaredMethod
import com.vk.tools.designanalysis.hierarchy.buildClassHierarchyFromClasspath
import com.vk.tools.designanalysis.instrumentation.AnalysisValidator
import com.vk.tools.designanalysis.instrumentation.PrimitivesDataflowAnalysis
import com.vk.tools.designanalysis.taint.PossiblyTaintedSource
import com.vk.tools.designanalysis.taint.findAllTainted
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassReader.EXPAND_FRAMES
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_MAXS
import java.io.InputStream
import java.io.OutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.io.path.Path

abstract class DesignInstrumentationTask : DefaultTask() {
    @get:InputFiles
    @get:Incremental
    abstract val inputJars: ConfigurableFileCollection

    @get:InputFiles
    @get:Incremental
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectories: ConfigurableFileCollection

    @get:InputFiles
    @get:Incremental
    abstract val taintEdgesDirectory: DirectoryProperty

    @get:InputFiles
    @get:Incremental
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val changedMethodsDirectory: DirectoryProperty

    @get:InputFiles
    abstract val bootClasspath: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val intoJarDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val intoDirDirectory: DirectoryProperty

    @TaskAction
    fun execute(changes: InputChanges) {
        val jarChanges = changes.getFileChanges(inputJars)
        val dirChanges = changes.getFileChanges(inputDirectories)
        val methodChanges = changes.getFileChanges(changedMethodsDirectory)

        val inputJars = inputJars.toList()
        val inputDirectories = inputDirectories.toList()
        val taintEdgesDirectory = taintEdgesDirectory.get().asFile
        val changedMethodsDirectory = changedMethodsDirectory.get().asFile
        val bootClasspath = bootClasspath.toList()

        val outputJarDirectory = intoJarDirectory.get().asFile
        val outputDirDirectory = intoDirDirectory.get().asFile

        if (!changes.isIncremental) {
            outputJarDirectory.deleteRecursively()
            outputDirDirectory.deleteRecursively()
            outputJarDirectory.mkdirs()
            outputDirDirectory.mkdirs()
        }

        val (classHierarchy, analysisValidator) = buildClassHierarchyFromClasspath(
            inputJars,
            inputDirectories,
            bootClasspath,
        )

        for (change in jarChanges) {
            if (change.changeType == ChangeType.REMOVED) {
                val jar = change.file
                val filename = jar.name
                val intoJar = outputJarDirectory.resolve(filename)
                intoJar.delete()
            }
        }

        for (change in dirChanges) {
            if (change.changeType == ChangeType.REMOVED) {
                val changeFilePath = change.file.toPath()
                val dir = changeFilePath.subpath(
                    0,
                    changeFilePath.nameCount - Path(change.normalizedPath).nameCount
                ).toFile()
                val filename = dir.name
                val intoDir = outputDirDirectory.resolve(filename)
                intoDir.deleteRecursively()
            }
        }

        val allTainted = findAllTainted(taintEdgesDirectory, classHierarchy)
        val allChangedMethods = mutableSetOf<DeclaredMethod>()
        for (file in changedMethodsDirectory.listFiles()) {
            file.forEachLine { line ->
                val method: DeclaredMethod = Json.decodeFromString(line)
                allChangedMethods.add(method)
            }
        }

        for (jar in inputJars) {
            val filename = jar.nameWithoutExtension
            val outputJar = outputJarDirectory.resolve("$filename.jar")
            val changedMethodsFile = changedMethodsDirectory.resolve("$filename.jar.txt")

            val isJarChanged = jarChanges.any { change ->
                change.file == jar
            } || methodChanges.any { change ->
                change.file == changedMethodsFile
            }
            if (!isJarChanged) {
                continue
            }

            outputJar.outputStream()
                .let(::JarOutputStream)
                .use { out ->
                    JarFile(jar).use { jarFile ->
                        for (entry in jarFile.entries()) {
                            if (entry.name.endsWith(".class")) {
                                out.putNextEntry(JarEntry(entry.name))
                                jarFile.getInputStream(entry).use { inputStream ->
                                    instrument(
                                        inputStream,
                                        out,
                                        analysisValidator,
                                        allTainted,
                                        allChangedMethods,
                                    )
                                }
                                out.closeEntry()
                            }
                        }
                    }
                }
        }

        for (dir in inputDirectories.single().listFiles()) {
            val filename = dir.name
            val outputDir = outputDirDirectory.resolve(filename)
            val changedMethodsFile = changedMethodsDirectory.resolve("$filename.txt")

            val isDirChanged = dirChanges.any { change ->
                change.file.relativeToOrNull(dir) != null
            } || methodChanges.any { change ->
                change.file == changedMethodsFile
            }
            if (!isDirChanged) {
                continue
            }

            dir.walkTopDown().forEach { file ->
                if (file.isFile && file.extension == "class") {
                    val relativePath = file.toRelativeString(dir)
                    val outputFile = outputDir.resolve(relativePath)
                    outputFile.parentFile.mkdirs()
                    file.inputStream().use { inputStream ->
                        outputFile.outputStream().use { outputStream ->
                            instrument(
                                inputStream,
                                outputStream,
                                analysisValidator,
                                allTainted,
                                allChangedMethods,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun instrument(
        inputStream: InputStream,
        outputStream: OutputStream,
        analysisValidator: AnalysisValidator,
        allTainted: Set<PossiblyTaintedSource>,
        allChangedMethods: Set<DeclaredMethod>,
    ) {
        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(COMPUTE_MAXS)
        val visitor = PrimitivesDataflowAnalysis(
            delegate = classWriter,
            validator = analysisValidator,
            isTainted = allTainted::contains,
            shouldInstrumentMethod = allChangedMethods::contains,
        )
        classReader.accept(visitor, EXPAND_FRAMES)
        outputStream.write(classWriter.toByteArray())
    }
}

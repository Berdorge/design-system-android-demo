package com.vk.tools.designanalysis

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.component.analytics.AnalyticsEnabledVariant
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.component.ComponentCreationConfig
import com.android.build.gradle.internal.dependency.AsmClassesTransform
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.vk.tools.designanalysis.instrumentation.DesignAnalysisInstrumentationFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

abstract class DesignAnalysisPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val androidComponents = project.getAndroidComponents()
        androidComponents.onVariants { variant ->
            variant.instrumentation.transformClassesWith(
                DesignAnalysisInstrumentationFactory::class.java,
                InstrumentationScope.ALL
            ) {}
            variant.instrumentation.setAsmFramesComputationMode(
                FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
            )
            val config = if (variant is AnalyticsEnabledVariant) {
                variant.delegate as ComponentCreationConfig
            } else {
                variant as ComponentCreationConfig
            }
            val analysisTask = project.tasks.register(
                "designAnalysis${variant.name.capitalize()}",
                DesignAnalysisTask::class.java,
            ) { task ->
                task.fileToProjectName.set(
                    variant.runtimeConfiguration.incoming
                        .artifactView { view ->
                            view.attributes {
                                it.attribute(
                                    AndroidArtifacts.ARTIFACT_TYPE,
                                    AndroidArtifacts.ArtifactType.ASM_INSTRUMENTED_JARS.type
                                )
                                AsmClassesTransform.getAttributesForConfig(config)
                                    .addAttributesToContainer(it)
                            }
                        }
                        .artifacts
                        .resolvedArtifacts
                )
                task.bootClasspath.setFrom(project.getBootClasspath())
                task.taintEdgesDirectory.set(project.layout.buildDirectory.dir("taint-edges/${variant.name}"))
                task.changedMethodsDirectory.set(project.layout.buildDirectory.dir("changed-methods/${variant.name}"))
            }
            val instrumentationTask = project.tasks.register(
                "designInstrumentation${variant.name.capitalize()}",
                DesignInstrumentationTask::class.java
            ) { task ->
                task.taintEdgesDirectory.set(analysisTask.flatMap { it.taintEdgesDirectory })
                task.changedMethodsDirectory.set(analysisTask.flatMap { it.changedMethodsDirectory })
                task.bootClasspath.setFrom(project.getBootClasspath())
            }
            ArtifactsUtils.toTransform(
                config.artifacts
                    .forScope(ScopedArtifacts.Scope.ALL)
                    .use(analysisTask),
                ScopedArtifact.CLASSES,
                DesignAnalysisTask::inputJars,
                DesignAnalysisTask::inputDirectories,
                DesignAnalysisTask::intoJarDirectory,
                DesignAnalysisTask::intoDirDirectory,
            )
            ArtifactsUtils.toTransform(
                config.artifacts
                    .forScope(ScopedArtifacts.Scope.ALL)
                    .use(instrumentationTask),
                ScopedArtifact.CLASSES,
                DesignInstrumentationTask::inputJars,
                DesignInstrumentationTask::inputDirectories,
                DesignInstrumentationTask::intoJarDirectory,
                DesignInstrumentationTask::intoDirDirectory
            )
        }
    }

    companion object {
        private fun Project.getAndroidComponents(): AndroidComponentsExtension<*, *, *> {
            return extensions.getByType(AndroidComponentsExtension::class.java)
        }

        private fun Project.getBootClasspath(): List<File> {
            return extensions.getByType(BaseExtension::class.java).bootClasspath
        }

        private fun String.capitalize() = replaceFirstChar { it.uppercase() }
    }
}

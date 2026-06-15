package com.vk.tools.designanalysis;

import com.android.build.api.artifact.ScopedArtifact;
import com.android.build.api.artifact.impl.ScopedArtifactsImpl;

import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;

import kotlin.jvm.functions.Function1;

public final class ArtifactsUtils {
    public static <T extends Task> void toTransform(
        ScopedArtifactsImpl.ScopedArtifactsOperationImpl<T> artifacts,
        ScopedArtifact type,
        Function1<? super T, ? extends ConfigurableFileCollection> inputJars,
        Function1<? super T, ? extends ConfigurableFileCollection> inputDirectories,
        Function1<? super T, ? extends DirectoryProperty> intoJarDirectory,
        Function1<? super T, ? extends DirectoryProperty> intoDirDirectory
    ) {
        artifacts.toTransform$gradle_core(
            type,
            inputJars,
            inputDirectories,
            intoJarDirectory,
            intoDirDirectory
        );
    }
}

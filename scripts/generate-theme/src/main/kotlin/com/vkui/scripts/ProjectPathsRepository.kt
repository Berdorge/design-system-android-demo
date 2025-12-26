package com.vkui.scripts

import java.nio.file.Path

fun createPathsRepository(): ProjectPathsRepository {
    return GitProjectPathsRepository()
}

interface ProjectPathsRepository {
    fun getThemeGenerationSourceSetPath(): Path
    fun getThemeGenerationResourcesPath(): Path
}

private class GitProjectPathsRepository : ProjectPathsRepository {
    private val projectRootDirectory by lazy {
        val gitProcess = Runtime.getRuntime()
            .exec("git rev-parse --show-toplevel")
        gitProcess.waitFor()
        val gitOutput = gitProcess.inputStream
            .bufferedReader(Charsets.UTF_8)
            .readText()
            .trim()
        Path.of(gitOutput)
    }

    override fun getThemeGenerationSourceSetPath(): Path {
        return projectRootDirectory.resolve(Path.of("app", "src", "main", "java"))
    }

    override fun getThemeGenerationResourcesPath(): Path {
        return projectRootDirectory.resolve(Path.of("app", "src", "main", "res-generated"))
    }
}

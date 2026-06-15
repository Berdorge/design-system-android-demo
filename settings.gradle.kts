pluginManagement {
    includeBuild("demo-theme-generator") {
        name = "demo-theme-generator"
    }
    includeBuild("analysis-plugin") {
        name = "analysis-plugin"
    }

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "VKUI Design System"

include(":analysis-runtime")
include(":demo-app")
include(":demo-components-compose")
include(":demo-components-view")
include(":demo-icons")

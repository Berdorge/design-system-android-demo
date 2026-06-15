plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        register("design-analysis-plugin") {
            id = "com.vk.plugin.designanalysis"
            implementationClass = "com.vk.tools.designanalysis.DesignAnalysisPlugin"
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin")
        }
    }
}

dependencies {
    implementation(libs.android.tools.build)

    implementation(libs.asm)
    implementation(libs.asm.tree)
    implementation(libs.asm.analysis)

    implementation(libs.kotlinx.serialization.json)
}

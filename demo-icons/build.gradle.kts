import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.vkui.icons"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
}

dependencies {
}

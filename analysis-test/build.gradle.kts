import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.vkui.analysis.test"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
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

    testOptions {
        unitTests {
            androidResources {
                isIncludeAndroidResources = true
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)

    implementation(project(":analysis-plugin"))

    testImplementation(libs.android.tools.build)
    testImplementation(project(":analysis-runtime"))

    testImplementation(libs.asm)
    testImplementation(libs.asm.tree)
    testImplementation(libs.asm.analysis)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)

    testImplementation("org.assertj:assertj-core:3.27.7")
}

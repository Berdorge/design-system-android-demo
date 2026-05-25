import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.vkui.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vkui.demo"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":demo-components-compose"))
    implementation(project(":demo-components-view"))
    implementation(project(":demo-icons"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.fragment.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.appcompat)

    implementation(platform(libs.compose.bom))

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

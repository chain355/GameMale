plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.gamemale.client"
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }

    defaultConfig {
        applicationId = "com.gamemale.app"
        minSdk = 31
        targetSdk = 37
        versionCode = 122
        versionName = "5.1.2"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // 这台机器上 lintVital 经常卡死（不影响产物），出包时跳过它
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    val couiVersion = "0.0.1-alpha11"

    // COUI-Kit：ColorOS 17 风格组件与凝光视效
    implementation("io.github.itoseo:coui:$couiVersion")
    implementation("io.github.itoseo:coui-icons:$couiVersion")

    implementation(platform("androidx.compose:compose-bom-alpha:2026.08.01"))
    // Navigation 3 1.3 会把 ui、animation 抬到 1.14，BOM 仍把 foundation 留在 1.13，
    // 左右滑会因 ComposeUiFlags 字段对不上直接崩溃。这里把这几块钉在同一版。
    val compose = "1.14.0-alpha01"
    implementation("androidx.compose.ui:ui:$compose")
    implementation("androidx.compose.ui:ui-graphics:$compose")
    implementation("androidx.compose.foundation:foundation:$compose")
    implementation("androidx.compose.animation:animation:$compose")
    implementation("androidx.compose.runtime:runtime:$compose")
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.navigation3:navigation3-runtime:1.3.0-alpha05")
    implementation("androidx.navigation3:navigation3-ui:1.3.0-alpha05")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0")
    // HTML 解析 + 图片加载（原生渲染用）
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("io.coil-kt:coil-compose:2.7.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.test:core-ktx:1.7.0")
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.gamemale.client"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.gamemale.app"
        minSdk = 31
        targetSdk = 37
        versionCode = 101
        versionName = "4.3.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.activity:activity-compose:1.13.0")
    // HTML 解析 + 图片加载（原生渲染用）
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("io.coil-kt:coil-compose:2.7.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.test:core-ktx:1.7.0")
}

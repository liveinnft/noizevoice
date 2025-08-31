plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.lionido.noizevoice"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lionido.noizevoice"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // LibGDX dependencies
    implementation("com.badlogicgames.gdx:gdx-backend-android:1.12.1")
    implementation("com.badlogicgames.gdx:gdx:1.12.1")
    implementation("com.badlogicgames.gdx:gdx-box2d:1.12.1")
    implementation("com.badlogicgames.gdx:gdx-freetype:1.12.1")

    // Natives для Android
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.1:natives-arm64-v8a")
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.1:natives-armeabi-v7a")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:1.12.1:natives-arm64-v8a")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:1.12.1:natives-armeabi-v7a")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:1.12.1:natives-arm64-v8a")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:1.12.1:natives-armeabi-v7a")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
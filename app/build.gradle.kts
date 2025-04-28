plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.LambdaProject.MathArt"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.LambdaProject.MathArt"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"

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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.datastore:datastore-preferences:1.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")
    implementation("androidx.compose.ui:ui-text-google-fonts")
    implementation("androidx.compose.foundation:foundation:1.5.4")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.animation:animation:1.7.8")
    implementation(platform("androidx.compose:compose-bom:2025.04.00"))
    implementation("com.google.accompanist:accompanist-navigation-animation:0.33.2-alpha")
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.google.dagger:hilt-android:2.56.1")
    ksp("com.google.dagger:hilt-android-compiler:2.56.1")
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:22.1.1")
    implementation("com.google.firebase:firebase-firestore:24.9.1")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    implementation("androidx.compose.ui:ui:1.0.0")
    implementation("androidx.compose.material:material:1.0.0")
    implementation("androidx.compose.material3:material3:1.4.0-alpha12")
    implementation("androidx.navigation:navigation-compose:2.4.0-alpha01")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
}
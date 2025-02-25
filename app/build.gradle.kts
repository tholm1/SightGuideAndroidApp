plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.sightguide"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sightguide"
        minSdk = 24
        targetSdk = 34
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.vision.common)
    implementation(libs.cameracore)
    implementation(libs.cameracamera2)
    implementation(libs.cameralifecycle)
    implementation(libs.cameraview)
    implementation(libs.lifecycleruntimektx)
    implementation(libs.objectdetectioncommon)
    implementation(libs.objectdetection)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.guava)
    implementation(libs.firebasebom)
    implementation(libs.firebaseanalytics)
    implementation(libs.firebasecrashlytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
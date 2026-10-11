plugins {
    alias(plugins.androidApplication)
    alias(plugins.kotlinAndroid)
    alias(plugins.composeCompiler)
    alias(plugins.ksp)
    alias(plugins.hilt)
}

android {
    namespace = "it.bollofacile"
    compileSdk = 35

    defaultConfig {
        applicationId = "it.bollofacile"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            matchingFallbacks = ["release"]
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=androidx.lifecycle.ExperimentalLifecycleApi",
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }

    packagingOptions {
        resources.excludes += listOf("META-INF/*.kotlin_module")
    }

    buildFeatures {
        compose = true
        viewBinding = false
        dataBinding = false
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }
}

dependencies {
    val libs = project.libs

    // Compose BOM
    implementation(platform(libs.composeBom))

    // Compose
    implementation(libs.composeUi)
    implementation(libs.composeMaterial3)
    implementation(libs.composeMaterialIcons)
    implementation(libs.composeActivity)
    implementation(libs.composeViewModel)
    implementation(libs.composeRuntime)
    implementation(libs.composeFoundation)
    implementation(libs.composeAnimation)
    implementation(libs.composeTooling)
    implementation(libs.composeToolingPreview)

    // Accompanist
    implementation(libs.accompanistPermissions)
    implementation(libs.accompanistInsets)

    // Hilt
    implementation(libs.hiltAndroid)
    implementation(libs.hiltNavigation)
    ksp(libs.hiltCompiler)

    // Room
    implementation(libs.roomRuntime)
    implementation(libs.roomKtx)
    ksp(libs.roomCompiler)

    // Lifecycle
    implementation(libs.lifecycleViewModel)
    implementation(libs.lifecycleRuntime)
    implementation(libs.lifecycleViewModelSavedState)

    // Navigation
    implementation(libs.navigationCompose)
    implementation(libs.navigationFragment)

    // Coroutines
    implementation(libs.coroutinesAndroid)

    // Kotlinx Datetime
    implementation(libs.kotlinxDatetime)

    // WorkManager
    implementation(libs.workManager)

    // Play Billing
    implementation(libs.playBilling)
    implementation(libs.playBillingKtx)

    // Ads & UMP
    implementation(libs.playServicesAds)
    implementation(libs.ump)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.junitJupiter)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutinesTest)

    androidTestImplementation(libs.espressoCore)
    androidTestImplementation(libs.espressoContrib)
    androidTestImplementation(libs.composeUiTest)
    androidTestImplementation(libs.composeUiTestManifest)
    androidTestImplementation(libs.junitJupiter)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.turbine)

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-tooling-preview")
}
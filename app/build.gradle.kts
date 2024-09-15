import java.io.FileInputStream
import java.util.Properties

plugins {
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.emsi.mediaserver"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.emsi.mediaserver"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders += mapOf(
            "redirectSchemeName" to "required.by.spotify.login.sdk",
            "redirectHostName" to "we.are.using.another.sdk.to.do.login"
        )

        val localProperties = Properties()
        localProperties.load(FileInputStream(rootProject.file("local.properties")))

        buildConfigField("String", "CLIENT_ID", localProperties["CLIENT_ID"] as String)
        buildConfigField("String", "REDIRECT_URL", localProperties["REDIRECT_URL"] as String)
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    implementation("androidx.activity:activity-compose:1.8.2") // do not update, keep on 1.8.2, unless spotify-api-kotlin-core updates
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.media3:media3-session:1.4.1")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.8.1")
    //for livedata
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
//for room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    implementation("androidx.lifecycle:lifecycle-runtime-compose-android:2.8.5")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("com.google.dagger:hilt-android:2.47")
    kapt("com.google.dagger:hilt-android-compiler:2.44")

    implementation(files("libs/spotify-app-remote-release-0.8.0.aar"))
//    implementation("com.spotify.android:auth:2.1.1")
    implementation("com.adamratzman:spotify-api-kotlin-core:4.1.3")

}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id ("androidx.navigation.safeargs.kotlin")
    id ("dagger.hilt.android.plugin")
    id ("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics")
}

android {
    namespace = "com.bluell.roomdecoration.interiordesign"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bluell.roomdecoration.interiordesign"
        minSdk = 24
        targetSdk = 34
        versionCode = 19
        versionName = "1.1.9"
        compileSdkPreview = "UpsideDownCake"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "Room Design - V$versionCode($versionName)")
    }

    bundle{
        language{
            enableSplit = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures{
        viewBinding = true
    }


    defaultConfig {
        multiDexEnabled = true

    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Activity KTX for viewModels()
    implementation ("androidx.activity:activity-ktx:1.7.2")

    // Coroutine Lifecycle Scopes
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    //sdp
    implementation("com.intuit.sdp:sdp-android:1.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    //data store
    implementation ("androidx.datastore:datastore-preferences:1.0.0")

    //room
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    implementation ("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
    implementation ("com.squareup.retrofit2:converter-scalars:2.1.0")
    implementation ("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.13.1")
    kapt("com.github.bumptech.glide:compiler:4.13.1")
    implementation("com.squareup.okhttp3:okhttp:3.14.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.13.1") {
        exclude(group = "glide-parent")
    }

    //lottie
    implementation ("com.airbnb.android:lottie:6.1.0")

    //cameraX
    val cameraxVersion = "1.3.2"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // Activity KTX for viewModels()
    implementation ("androidx.activity:activity-ktx:1.8.2")

    //Dagger - Hilt
    implementation ("com.google.dagger:hilt-android:2.51.1")
    kapt ("com.google.dagger:hilt-android-compiler:2.51.1")

    //    firebase
    implementation ("com.google.firebase:firebase-messaging:23.4.1")
    implementation ("com.google.firebase:firebase-database:20.3.1")
    implementation ("com.google.firebase:firebase-storage:20.3.0")
    implementation ("com.google.firebase:firebase-analytics:21.6.1")
    implementation ("com.android.billingclient:billing:6.2.0")
    implementation ("com.google.firebase:firebase-common-ktx:20.4.3")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.11.0")
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
//    implementation("com.google.android.gms:play-services-ads:23.0.0")
//    implementation("com.google.android.ump:user-messaging-platform:2.2.0")

    implementation("com.android.installreferrer:installreferrer:2.2")

    //chip navigation
    implementation ("com.github.ismaeldivita:chip-navigation-bar:1.4.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.mmmelik:RoundedImageView:v1.0.1")
    implementation ("com.github.symeonchen:roundImageView:0.1.0")

    //shimmer
    implementation ("io.supercharge:shimmerlayout:2.1.0")

    //compressor
    implementation ("com.github.Shouheng88:compressor:1.6.0")

    //zoomable ImageView
    implementation ("com.github.chrisbanes:PhotoView:2.0.0")

}
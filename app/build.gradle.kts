    plugins {
        id("com.android.application")
        id("org.jetbrains.kotlin.android")
        id("com.google.gms.google-services")
    }

    android {
        namespace = "com.inventory.tfgproject"
        compileSdk = 34

        defaultConfig {
            applicationId = "com.inventory.tfgproject"
            minSdk = 26
            targetSdk = 34
            versionCode = 1
            versionName = "1.0"

            vectorDrawables.useSupportLibrary = true
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            multiDexEnabled = true


            renderscriptTargetApi = 21
            renderscriptSupportModeEnabled = true

            ndk {
                abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            }

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
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        kotlinOptions {
            jvmTarget = "1.8"
        }
        buildFeatures {
            viewBinding = true
            dataBinding = true
        }

        packagingOptions {
            resources {
                excludes += listOf(
                    "/apex/com.android.extservices/javalib/android.ext.adservices.dm",
                    "META-INF/DEPENDENCIES",
                    "META-INF/LICENSE",
                    "META-INF/LICENSE.txt",
                    "META-INF/license.txt",
                    "META-INF/NOTICE",
                    "META-INF/NOTICE.txt",
                    "META-INF/notice.txt",
                    "META-INF/*.kotlin_module"
                )
            }
            jniLibs {
                useLegacyPackaging = true
            }
        }

        dexOptions {
            javaMaxHeapSize = "4g"
            preDexLibraries = false
            additionalParameters.plusAssign("--minimal-main-dex")
        }
    }


    dependencies {
        //Fire Base
        implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
        implementation("com.google.firebase:firebase-analytics")
        implementation("com.google.firebase:firebase-database:21.0.0")
        implementation("com.google.firebase:firebase-auth-ktx:23.1.0")
        implementation("com.google.android.gms:play-services-auth:21.3.0")
        implementation("androidx.activity:activity:1.9.3")
        implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
        implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
        implementation("androidx.recyclerview:recyclerview:1.3.2")
        implementation ("com.github.bumptech.glide:glide:4.16.0")
        implementation("com.google.firebase:firebase-appcheck-debug:18.0.0")
        implementation("androidx.metrics:metrics-performance:1.0.0-beta01")
        implementation ("androidx.core:core-ktx:1.12.0")
        implementation("androidx.test.espresso:espresso-core:3.6.1")
        implementation ("androidx.work:work-runtime-ktx:2.8.1")
        implementation("com.google.android.gms:play-services-fido:20.0.1")
        implementation ("org.apache.poi:poi:5.2.3")
        implementation ("org.apache.poi:poi-ooxml:5.2.3")
        //Lottie
        val lottieVersion = "6.0.0"
        implementation("com.airbnb.android:lottie-compose:$lottieVersion")
        //Fragment
        implementation("androidx.fragment:fragment-ktx:1.8.5")
        //Activity
        implementation("androidx.activity:activity-ktx:1.9.3")
        //ViewModel
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
        //LiveData
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
        //CircleImageView
        implementation("de.hdodenhof:circleimageview:3.1.0")
        //CameraBarCode
        val camerax_version = "1.4.1"
        implementation ("androidx.camera:camera-core:${camerax_version}")
        implementation ("androidx.camera:camera-camera2:${camerax_version}")
        implementation ("androidx.camera:camera-lifecycle:${camerax_version}")
        implementation ("androidx.camera:camera-view:${camerax_version}")

        implementation ("com.google.android.gms:play-services-vision:20.1.3")
        implementation ("com.codesgood:justifiedtextview:1.1.0")
        implementation ("com.google.firebase:firebase-appcheck-playintegrity:18.0.0")
        implementation ("com.google.android.play:integrity:1.4.0")
        implementation ("com.google.firebase:firebase-appcheck-debug")
        implementation ("com.google.firebase:firebase-appcheck:18.0.0")
        implementation ("com.google.firebase:firebase-appcheck-debug:18.0.0")
        implementation ("com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.1")
        implementation ("androidx.camera:camera-core:1.4.1")
        implementation ("androidx.camera:camera-view:1.4.1")
        implementation ("androidx.camera:camera-lifecycle:1.4.1")

        implementation ("androidx.appcompat:appcompat:1.7.0")
        implementation ("com.google.android.material:material:1.12.0")
        implementation ("androidx.constraintlayout:constraintlayout:2.2.0")
        implementation ("androidx.compose.animation:animation-core-android:1.7.6")
        implementation("androidx.multidex:multidex:2.0.1")
        testImplementation ("junit:junit:4.13.2")
        androidTestImplementation ("androidx.test.ext:junit:1.2.1")
        androidTestImplementation ("androidx.test.espresso:espresso-core:3.6.1")

    }


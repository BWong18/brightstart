plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.brightstart"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.brightstart"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:21.0.1") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
    implementation("com.google.firebase:firebase-firestore:24.0.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
    implementation("com.google.firebase:firebase-storage:20.0.1") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }

    // Kotlin standard library (only include kotlin-stdlib)
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.12.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
    implementation(libs.activity)
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // AndroidX dependencies
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity-ktx:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core:1.9.0")
    implementation("androidx.media:media:1.0.0")

    // Material Calendar View library
    implementation("com.prolificinteractive:material-calendarview:1.4.3") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

configurations.all {
    resolutionStrategy {
        // Exclude kotlin-stdlib-jdk8 globally
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
        // Ensure only one Kotlin stdlib version is used
        force("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")
    }
}

buildscript {
    repositories {
        google()  // Repositories are needed here for classpath resolution
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle)  // Make sure you're using the correct version
        classpath("com.google.gms:google-services:4.4.2")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}



plugins {
    id("com.lagradost.cloudstream3.gradle") version "1.0.0"
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://jitpack.io")
    }
}

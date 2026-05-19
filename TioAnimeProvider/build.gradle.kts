plugins {
    id("com.android.library")
    kotlin("android")
}

apply(plugin = "com.lagradost.cloudstream3.gradle")

android {
    namespace = "com.godusghosts.tioanime"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }
}

cloudstream {
    language = "es"
    authors = listOf("GodusGhosts")
    status = 1

    tvTypes = listOf(
        "Anime"
    )

    description = "Anime sub español latino desde TioAnime"
    iconUrl = "https://tioanime.com/favicon.ico"
}

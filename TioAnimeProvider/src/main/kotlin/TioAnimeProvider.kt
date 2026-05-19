package com.godusghosts.tioanime

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class TioAnimeProvider : MainAPI() {

    override var mainUrl = "https://tioanime.com"
    override var name = "TioAnime"
    override val hasMainPage = true
    override var lang = "es"

    override val supportedTypes = setOf(TvType.Anime)

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/directorio?q=$query").document

        return document.select("article").mapNotNull { element ->
            element.toSearchResult()
        }
    }

    private fun Element.toSearchResult(): AnimeSearchResponse? {
        val title = selectFirst("h3")?.text() ?: return null
        val href = fixUrl(selectFirst("a")?.attr("href") ?: return null)
        val poster = selectFirst("img")?.attr("src")

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            posterUrl = poster
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title = document.selectFirst("h1")?.text() ?: "Sin título"
        val description = document.selectFirst(".sinopsis")?.text()

        val slug = url.substringAfterLast("/anime/")
        val poster = "$mainUrl/uploads/animes/covers/$slug.jpg"

        val episodes = (1..50).map { number ->
            newEpisode("$mainUrl/ver/$slug-$number") {
                name = "Episodio $number"
                episode = number
            }
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            posterUrl = poster
            plot = description
        }.apply {
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document

        val iframe = document.selectFirst("iframe")?.attr("src") ?: return false

        loadExtractor(
            iframe,
            mainUrl,
            subtitleCallback,
            callback
        )

        return true
    }
}

@CloudstreamPlugin
class TioAnimePlugin : Plugin() {
    override fun load(context: android.content.Context) {
        registerMainAPI(TioAnimeProvider())
    }
}

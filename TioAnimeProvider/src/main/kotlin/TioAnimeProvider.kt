package com.godusghosts.tioanime

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class TioAnimeProvider : MainAPI() {

    override var mainUrl = "https://tioanime.com"
    override var name = "TioAnime"
    override val hasMainPage = true
    override var lang = "es"

    override val supportedTypes = setOf(
        TvType.Anime
    )

    override suspend fun search(query: String): List<SearchResponse> {

        val document = app.get(
            "$mainUrl/directorio?q=$query"
        ).document

        return document.select("article").mapNotNull { element ->
            element.toSearchResult()
        }
    }

    private fun Element.toSearchResult(): AnimeSearchResponse? {

        val title = this.selectFirst("h3")?.text()
            ?: return null

        val href = fixUrl(
            this.selectFirst("a")
                ?.attr("href")
                ?: return null
        )

        val poster = this.selectFirst("img")
            ?.attr("src")

        return newAnimeSearchResponse(
            title,
            href,
            TvType.Anime
        ) {
            this.posterUrl = poster
        }
    }

    override suspend fun load(url: String): LoadResponse {

        val document = app.get(url).document

        val title = document.selectFirst("h1")
            ?.text()
            ?: "Sin título"

        val poster = document.selectFirst("img")
            ?.attr("src")

        val description = document.selectFirst(".sinopsis")
            ?.text()

        val episodes = document.select("li").mapNotNull { element ->

            val link = element.selectFirst("a")
                ?: return@mapNotNull null

            val epUrl = fixUrl(
                link.attr("href")
            )

            Episode(
                data = epUrl,
                name = link.text()
            )
        }

        return newAnimeLoadResponse(
            title,
            url,
            TvType.Anime
        ) {
            posterUrl = poster
            plot = description
            this.episodes = episodes
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {

        val document = app.get(data).document

        val iframe = document.selectFirst("iframe")
            ?.attr("src")
            ?: return false

        callback.invoke(
            ExtractorLink(
                source = name,
                name = "TioAnime",
                url = iframe,
                referer = mainUrl,
                quality = Qualities.Unknown.value,
                isM3u8 = iframe.contains(".m3u8")
            )
        )

        return true
    }
}

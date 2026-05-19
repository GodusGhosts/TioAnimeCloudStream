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

    override val mainPage = mainPageOf(
        "$mainUrl/directorio?q=" to "Anime"
    )

    override suspend fun search(query: String): List<SearchResponse> {

        val document = app.get(
            "$mainUrl/directorio?q=$query"
        ).document

        return document.select("article").mapNotNull {
            it.toSearchResult()
        }
    }

    private fun Element.toSearchResult(): AnimeSearchResponse? {

        val title = this.selectFirst("h3")?.text() ?: return null

        val href = fixUrl(
            this.selectFirst("a")?.attr("href") ?: return null
        )

        val poster = fixUrlNull(
            this.selectFirst("img")?.attr("src")
        )

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

        val title = document.selectFirst("h1")?.text()
            ?: "Sin título"

        val poster = fixUrlNull(
            document.selectFirst(".anime-info img")
                ?.attr("src")
        )

        val description = document.selectFirst(".sinopsis")
            ?.text()

        val episodes = document.select("ul.episodes li").map {
            val epUrl = fixUrl(
                it.selectFirst("a")!!.attr("href")
            )

            val epName = it.text()

            Episode(
                data = epUrl,
                name = epName
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
                source = "TioAnime",
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

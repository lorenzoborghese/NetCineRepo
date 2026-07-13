package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.Jsoup

class Nnn1Provider : MainAPI() {
    override var mainUrl = "https://nnn1.lat"
    override var name = "NetCine Nnn1"
    override val supportedTypes = setOf(TvType.TvSeries, TvType.Movie)

    // 1. FUNÇÃO DE PESQUISA (Search)
    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val html = app.get(url).text
        val document = Jsoup.parse(html)

        return document.select("article.item").mapNotNull { element ->
            val title = element.selectFirst(".title a")?.text() ?: return@mapNotNull null
            val link = element.selectFirst(".title a")?.attr("href") ?: return@mapNotNull null
            val poster = element.selectFirst(".poster img")?.attr("src") ?: element.selectFirst(".poster img")?.attr("data-src")
            
            val isTv = link.contains("/tvshows/")
            
            if (isTv) {
                newTvSeriesSearchResponse(title, link, TvType.TvSeries) {
                    this.posterUrl = poster
                }
            } else {
                newMovieSearchResponse(title, link, TvType.Movie) {
                    this.posterUrl = poster
                }
            }
        }
    }

    // 2. FUNÇÃO DE CARREGAMENTO (Load)
    override suspend fun load(url: String): LoadResponse {
        val html = app.get(url).text
        val document = Jsoup.parse(html)

        val title = document.selectFirst("h1")?.text() ?: document.selectFirst(".data h1")?.text() ?: "Sem Título"
        val poster = document.selectFirst(".poster img")?.attr("src")
        val description = document.selectFirst(".wp-content p")?.text()

        return if (url.contains("/tvshows/")) {
            val episodes = mutableListOf<Episode>()
            
            document.select(".episodios li").forEach { element ->
                val epLink = element.selectFirst("a")?.attr("href") ?: return@forEach
                val epName = element.selectFirst(".epst")?.text() ?: element.selectFirst("a")?.text() ?: ""
                
                val seasonNumber = url.substringAfter("-season-", "").substringBefore("/").toIntOrNull() ?: 1
                val episodeNumber = epLink.substringAfter("-episode-", "").substringBefore("/").toIntOrNull()

                val episodeObj = newEpisode(epLink) {
                    this.name = epName
                    this.season = seasonNumber
                    this.episode = episodeNumber
                }
                
                episodes.add(episodeObj)
            }

            newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.plot = description
            }
        } else {
            newMovieLoadResponse(title, url, TvType.Movie, url) {
                this.posterUrl = poster
                this.plot = description
            }
        }
    }

    // 3. FUNÇÃO DE EXTRAÇÃO DE LINKS DE VÍDEO (LoadLinks)
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        // Seu código de carregar links continua aqui...
        return true
    }
}

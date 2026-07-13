package com.netcine

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.getQualityFromName
import org.jsoup.nodes.Element

class NetCineProvider : MainAPI() {
    override var mainUrl = "https://nnn1.lat"
    override var name = "NetCine"
    override val hasMainPage = true
    override var lang = "pt"
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = app.get(mainUrl).document
        val homePageList = ArrayList<HomePageList>()

        document.select("section").forEach { section ->
            val title = section.select("h2").text() ?: return@forEach
            val items = section.select("article").mapNotNull { it.toSearchResult() }
            if (items.isNotEmpty()) {
                homePageList.add(HomePageList(title, items))
            }
        }

        return HomePageResponse(homePageList)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.select("h3").text() ?: return null
        val href = this.select("a").attr("href") ?: return null
        val posterUrl = this.select("img").attr("src") ?: ""
        val year = this.select(".year").text().toIntOrNull()

        return MovieSearchResponse(
            name = title,
            url = href,
            apiName = this@NetCineProvider.name,
            type = TvType.Movie,
            posterUrl = posterUrl,
            year = year
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/busca?q=$query"
        val document = app.get(url).document

        return document.select("article").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.select("h1").text() ?: return null
        val description = document.select("p.description").text()
        val posterUrl = document.select("img.poster").attr("src") ?: ""
        val year = document.select(".year").text().toIntOrNull()

        val episodes = document.select("a.episode").mapNotNull {
            val episodeUrl = it.attr("href") ?: return@mapNotNull null
            val episodeTitle = it.text()
            Episode(episodeUrl, name = episodeTitle)
        }

        return if (episodes.isEmpty()) {
            MovieLoadResponse(
                name = title,
                url = url,
                apiName = name,
                type = TvType.Movie,
                posterUrl = posterUrl,
                year = year,
                plot = description
            )
        } else {
            TvSeriesLoadResponse(
                name = title,
                url = url,
                apiName = name,
                type = TvType.TvSeries,
                episodes = listOf(EpisodeData(episodes)),
                posterUrl = posterUrl,
                year = year,
                plot = description
            )
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        val videoUrl = document.select("video source").attr("src") ?: return false

        callback(
            ExtractorLink(
                source = name,
                name = name,
                url = videoUrl,
                referer = mainUrl,
                quality = Qualities.Unknown.value,
                type = INFER_TYPE
            )
        )

        return true
    }
}
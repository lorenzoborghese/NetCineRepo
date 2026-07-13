package com.lagradost

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.SearchResponse

class Nnn1Provider : MainAPI() {
    override var mainUrl = "https://nnn1.lat"
    override var name = "NetCine Nnn1"
    override val supportedTypes = setOf(TvType.TvSeries, TvType.Movie)
    
    // Aqui dentro entram as funções de busca e carregamento
}

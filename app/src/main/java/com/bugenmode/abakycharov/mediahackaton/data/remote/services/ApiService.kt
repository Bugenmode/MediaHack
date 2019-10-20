package com.bugenmode.abakycharov.mediahackaton.data.remote.services

import com.bugenmode.abakycharov.mediahackaton.data.remote.model.Articles
import retrofit2.Response
import retrofit2.http.POST

interface ApiService {

    @POST("/hide/media/json.php")
    suspend fun getAllArticles() : Response<List<Articles>>
}
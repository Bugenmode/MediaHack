package com.bugenmode.abakycharov.mediahackaton.data.repository

import com.bugenmode.abakycharov.mediahackaton.data.remote.model.Articles
import com.bugenmode.abakycharov.mediahackaton.data.remote.services.ApiService
import retrofit2.Response

class ArticleRepository(
    private val apiService: ApiService
) {

    suspend fun getArticles() : Response<List<Articles>> {
        return apiService.getAllArticles()
    }
}
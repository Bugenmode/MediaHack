package com.bugenmode.abakycharov.mediahackaton.di.modules

import com.bugenmode.abakycharov.mediahackaton.data.remote.services.ApiService
import com.bugenmode.abakycharov.mediahackaton.data.repository.ArticleRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun provideArticleRepository(
        apiService: ApiService
    ) : ArticleRepository {
        return ArticleRepository(apiService)
    }
}
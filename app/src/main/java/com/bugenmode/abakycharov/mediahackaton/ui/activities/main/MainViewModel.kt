package com.bugenmode.abakycharov.mediahackaton.ui.activities.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bugenmode.abakycharov.mediahackaton.data.remote.model.Articles
import com.bugenmode.abakycharov.mediahackaton.data.repository.ArticleRepository
import com.bugenmode.abakycharov.mediahackaton.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class MainViewModel @Inject constructor(
    val articleRepository: ArticleRepository
) : BaseViewModel() {

    var articles = MutableLiveData<MutableList<Articles>>()

    fun getAllArticles() {
        runBlocking {
            launch {
                val r = articleRepository.getArticles()

                try {
                    if (r.isSuccessful) {
                        Timber.d(r.body().toString())
                        articles.value = r.body()?.toMutableList()

                        sendEvent("loaded")
                    }
                } catch (ex: Exception) {
                    Timber.d(ex)
                }
            }
        }
    }
}
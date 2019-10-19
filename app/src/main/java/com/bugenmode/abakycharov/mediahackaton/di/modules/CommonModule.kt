package com.bugenmode.abakycharov.mediahackaton.di.modules

import android.app.Application
import com.bugenmode.abakycharov.mediahackaton.App
import com.bugenmode.abakycharov.mediahackaton.utils.DateDeserializer
import com.bugenmode.abakycharov.mediahackaton.utils.DateSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import org.threeten.bp.LocalDateTime
import javax.inject.Singleton

@Module
class CommonModule {

    @Provides
    @Singleton
    fun provideApp(application: Application): App {
        return application as App
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, DateSerializer())
            .registerTypeAdapter(LocalDateTime::class.java, DateDeserializer())
            .create()
    }

}
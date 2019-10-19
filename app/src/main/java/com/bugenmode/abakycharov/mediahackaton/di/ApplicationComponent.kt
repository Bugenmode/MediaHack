package com.bugenmode.abakycharov.mediahackaton.di

import android.app.Application
import com.bugenmode.abakycharov.mediahackaton.App
import com.bugenmode.abakycharov.mediahackaton.di.modules.CommonModule
import com.bugenmode.abakycharov.mediahackaton.di.modules.NetworkModule
import com.bugenmode.abakycharov.mediahackaton.ui.activities.main.MainViewModel
import com.bugenmode.abakycharov.mediahackaton.ui.activities.maps.MapsViewModel
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        AndroidSupportInjectionModule::class,
        CommonModule::class
    ]
)
interface ApplicationComponent {

    @Component.Builder
    interface Builder {
        fun build(): ApplicationComponent
        @BindsInstance
        fun application(application: Application): Builder
    }

    fun inject(app: App)

    fun vmfMainMenu(): ViewModelFactory<MainViewModel>
    fun vmfMaps(): ViewModelFactory<MapsViewModel>
}
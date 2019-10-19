package com.bugenmode.abakycharov.mediahackaton

import android.app.Application
import android.app.Service
import com.bugenmode.abakycharov.mediahackaton.di.ApplicationComponent
import com.bugenmode.abakycharov.mediahackaton.di.DaggerApplicationComponent
import com.bugenmode.abakycharov.mediahackaton.di.DaggerComponentProvider
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasServiceInjector
import timber.log.Timber
import javax.inject.Inject

class App :
    Application(), DaggerComponentProvider, HasServiceInjector {
    @Inject
    lateinit var serviceDispatchingAndroidInjector: DispatchingAndroidInjector<Service>

    override fun serviceInjector(): AndroidInjector<Service> = serviceDispatchingAndroidInjector

    override val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder()
            .application(this)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
        setupTimber()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.uprootAll()
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    var newTag = tag
                    if (tag != null) newTag = "BUGENMODE: $tag"
                    super.log(priority, newTag, message, t)
                }
            })
            Timber.i("App created")
        }
    }
}
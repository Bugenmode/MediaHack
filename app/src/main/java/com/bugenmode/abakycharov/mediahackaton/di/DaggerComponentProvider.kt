package com.bugenmode.abakycharov.mediahackaton.di

import android.app.Activity
import androidx.fragment.app.Fragment

interface DaggerComponentProvider {

    val component: ApplicationComponent
}

val Activity.injector get() = (application as DaggerComponentProvider).component
val Fragment.injector get() = (requireActivity().application as DaggerComponentProvider).component
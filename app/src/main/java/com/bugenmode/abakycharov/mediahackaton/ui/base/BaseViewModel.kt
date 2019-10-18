package com.bugenmode.abakycharov.mediahackaton.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bugenmode.abakycharov.mediahackaton.utils.Event
import com.bugenmode.abakycharov.mediahackaton.utils.EventData

open class BaseViewModel : ViewModel() {

    val events = MutableLiveData<Event<EventData>>()

    fun sendEvent(code: String?, payload: Any? = null) {
        val eventData = EventData(eventCode = code, eventPayload = payload)
        //must be used setValue instead of postValue. setValue uses event queue
        events.value = Event(eventData)
    }

}
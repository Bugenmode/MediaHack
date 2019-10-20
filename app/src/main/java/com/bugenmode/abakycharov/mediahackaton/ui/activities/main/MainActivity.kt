package com.bugenmode.abakycharov.mediahackaton.ui.activities.main

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bugenmode.abakycharov.mediahackaton.R
import com.bugenmode.abakycharov.mediahackaton.data.local.model.CardModel
import com.bugenmode.abakycharov.mediahackaton.data.remote.model.Articles
import com.bugenmode.abakycharov.mediahackaton.databinding.ActivityMainBinding
import com.bugenmode.abakycharov.mediahackaton.di.injector
import com.bugenmode.abakycharov.mediahackaton.ui.activities.detail.DetailActivity
import com.bugenmode.abakycharov.mediahackaton.ui.adapters.CardAdapter
import com.bugenmode.abakycharov.mediahackaton.ui.base.BaseActivity
import com.bugenmode.abakycharov.mediahackaton.utils.EventData
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import kotlinx.android.synthetic.main.item_cards.view.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity(), CardAdapter.OnItemClickListener {

    lateinit var b: ActivityMainBinding
    private lateinit var cardAdapter: CardAdapter

    private var TTS: TextToSpeech? = null
    private var ttsEnabled: Boolean = false

    private var array: ArrayList<Articles> = ArrayList()

    val viewModel by lazy {
        ViewModelProvider(this, injector.vmfMainMenu()).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupEventListener(this, viewModel)

        b = DataBindingUtil.setContentView(this, R.layout.activity_main)
        b.lifecycleOwner = this
        b.vm = viewModel

        viewModel.getAllArticles()

        initTextToSpeech()

        b.imgPlay.setOnClickListener {
            if (viewModel.articles.value != null) {
                speak(viewModel.articles.value?.get(0)?.short_txt!!)
            } else {
                Toast.makeText(this, "Данные еще не погружены. Пожалуйста дождитесь", Toast.LENGTH_LONG).show()
            }
        }

        b.imgLike.setOnClickListener {
            Toast.makeText(this, "Спасибо за отзыв!", Toast.LENGTH_LONG).show()
        }

        b.imgDontShow.setOnClickListener {
            Toast.makeText(this, "Благодарим за содействие, больше не будем показывать новости данной категории", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupList() {

        cardAdapter = CardAdapter(applicationContext, viewModel.articles.value!!, this)

        b.frame.adapter = cardAdapter

        b.frame.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {
            override fun removeFirstObjectInAdapter() {
            }

            override fun onLeftCardExit(p0: Any?) {
                viewModel.articles.value?.remove(p0)
                cardAdapter.notifyDataSetChanged()
            }

            override fun onRightCardExit(p0: Any?) {
                viewModel.articles.value?.remove(p0)
                cardAdapter.notifyDataSetChanged()
            }

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {}

            override fun onScroll(scrollProgressPercent: Float) {
                val view = b.frame.selectedView
                view.findViewById<FrameLayout>(R.id.background).alpha = 0.0f
                view.findViewById<View>(R.id.item_swipe_right_indicator).alpha =
                    if (scrollProgressPercent < 0) {
                        -scrollProgressPercent
                    } else {
                        0.0f
                    }

                view.findViewById<View>(R.id.item_swipe_left_indicator).alpha =
                    if (scrollProgressPercent > 0) {
                        scrollProgressPercent
                    } else {
                        0.0f
                    }
            }
        })

        b.frame.setOnItemClickListener { p0, p1 ->
            val view = b.frame.selectedView
            view.findViewById<FrameLayout>(R.id.background).alpha = 0.0f
            cardAdapter.notifyDataSetChanged()
        }
    }

    override fun OnItemClick(url: String) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    override fun onEvent(eventData: EventData) {
        when (eventData.eventCode) {
            "loaded" -> setupList()
        }
    }


    private fun initTextToSpeech() {

        TTS = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                if (TTS?.isLanguageAvailable(Locale(Locale.getDefault().language))
                    == TextToSpeech.LANG_AVAILABLE
                ) {
                    TTS?.language = Locale(Locale.getDefault().language)
                } else {
                    TTS?.language = Locale.US
                }
                TTS?.setPitch(1.3f)
                TTS?.setSpeechRate(0.7f)
                ttsEnabled = true
            } else {
                Toast.makeText(applicationContext, "SOME ERROR", Toast.LENGTH_LONG).show()
                ttsEnabled = false
            }
        })
    }

    private fun speak(text: String) {
        if (!ttsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(text)
        } else {
            ttsUnder20(text)
        }
    }

    @SuppressWarnings("deprecation")
    private fun ttsUnder20(text: String) {
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "MessageId"
        TTS?.speak(text, TextToSpeech.QUEUE_FLUSH, map)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun ttsGreater21(text: String) {
        val utteranceId = this.hashCode().toString() + ""
        TTS?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (TTS != null) {
            TTS!!.stop()
            TTS!!.shutdown()
            Timber.d("TTS Destroyed");
        }
    }

}

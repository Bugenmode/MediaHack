package com.bugenmode.abakycharov.mediahackaton.ui.activities.detail

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.bugenmode.abakycharov.mediahackaton.R
import com.bugenmode.abakycharov.mediahackaton.databinding.ActivityDetailBinding
import com.bugenmode.abakycharov.mediahackaton.di.injector
import com.bugenmode.abakycharov.mediahackaton.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_detail.*
import timber.log.Timber

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class DetailActivity : BaseActivity() {

    lateinit var b: ActivityDetailBinding

    val viewModel by lazy {
        ViewModelProvider(this, injector.vmfDetail()).get(DetailViewModel::class.java)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupEventListener(this, viewModel)

        b = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        b.lifecycleOwner = this
        b.vm = viewModel

        b.webView.clearCache(true)
        b.webView.settings?.javaScriptEnabled = true

        if (intent.getStringExtra("url") != null) {
            b.webView.loadUrl(intent.getStringExtra("url").toString())
        }

        b.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                b.progressBar.visibility = View.GONE
            }
        }
    }
}
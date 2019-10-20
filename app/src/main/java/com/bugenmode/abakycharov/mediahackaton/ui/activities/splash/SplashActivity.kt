package com.bugenmode.abakycharov.mediahackaton.ui.activities.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.bugenmode.abakycharov.mediahackaton.R
import com.bugenmode.abakycharov.mediahackaton.ui.activities.maps.MapsActivity
import com.bugenmode.abakycharov.mediahackaton.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        gif.setFreezesAnimation(true)

        Handler().postDelayed({
            val intent = Intent(this@SplashActivity, MapsActivity::class.java)
            startActivity(intent)
        }, 2000)
    }
}
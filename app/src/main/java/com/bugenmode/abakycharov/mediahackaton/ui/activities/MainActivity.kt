package com.bugenmode.abakycharov.mediahackaton.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bugenmode.abakycharov.mediahackaton.R
import com.bugenmode.abakycharov.mediahackaton.ui.base.BaseActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

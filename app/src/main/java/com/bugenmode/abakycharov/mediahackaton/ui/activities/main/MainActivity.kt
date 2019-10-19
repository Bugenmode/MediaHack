package com.bugenmode.abakycharov.mediahackaton.ui.activities.main

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bugenmode.abakycharov.mediahackaton.R
import com.bugenmode.abakycharov.mediahackaton.data.local.model.CardModel
import com.bugenmode.abakycharov.mediahackaton.databinding.ActivityMainBinding
import com.bugenmode.abakycharov.mediahackaton.di.injector
import com.bugenmode.abakycharov.mediahackaton.ui.adapters.CardAdapter
import com.bugenmode.abakycharov.mediahackaton.ui.base.BaseActivity
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import kotlinx.android.synthetic.main.item_cards.view.*

class MainActivity : BaseActivity() {

    lateinit var b: ActivityMainBinding
    private lateinit var cardAdapter: CardAdapter
    private lateinit var flingContainer : SwipeFlingAdapterView

    private var array: ArrayList<CardModel> = ArrayList()

    val viewModel by lazy {
        ViewModelProvider(this, injector.vmfMainMenu()).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupEventListener(this, viewModel)

        b = DataBindingUtil.setContentView(this, R.layout.activity_main)
        b.lifecycleOwner = this
        b.vm = viewModel

        flingContainer = b.frame

        array.add(
            CardModel(
                "http://www.delaroystudios.com/images/1.jpg",
                "Alexis Sanchez, Arsenal forward. Wanna chat with me ?. \n" +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            )
        )
        array.add(
            CardModel(
                "http://www.delaroystudios.com/images/2.jpg",
                "Christano Ronaldo, Real Madrid star. Wanna chat with me ? \n" +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            )
        )
        array.add(
            CardModel(
                "http://www.delaroystudios.com/images/3.jpg",
                "Lionel Messi, Barcelona Best player. Wanna chat with me ? \n" +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            )
        )
        array.add(
            CardModel(
                "http://www.delaroystudios.com/4.jpg", "David Beckham. Wanna chat with me ? \n" +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            )
        )
        array.add(
            CardModel(
                "http://www.delaroystudios.com/images/5.jpg",
                "Sergio Arguerio. Wanna chat with me ? \n" +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            )
        )
        array.add(
            CardModel(
                "http://www.delaroystudios.com/images/6.jpg",
                "Sergio Ramos. Wanna chat with me ? \n" +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            )
        )
        array.add(
            CardModel(
                "http://www.delaroystudios.com/images/7.jpg",
                "Robert Lewandoski. Wanna chat with me ? \n" +
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            )
        )


        cardAdapter = CardAdapter(this, array)

        flingContainer.adapter = cardAdapter

        flingContainer.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {
            override fun removeFirstObjectInAdapter() {
            }

            override fun onLeftCardExit(p0: Any?) {
                array.removeAt(0)
                cardAdapter.notifyDataSetChanged()
            }

            override fun onRightCardExit(p0: Any?) {
                array.removeAt(0)
                cardAdapter.notifyDataSetChanged()
            }

            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {}

            override fun onScroll(scrollProgressPercent: Float) {
                val view = flingContainer.selectedView
                view.findViewById<FrameLayout>(R.id.background).alpha = 0.0f
                view.findViewById<View>(R.id.item_swipe_right_indicator).alpha = if (scrollProgressPercent < 0) {
                    -scrollProgressPercent
                } else {
                    0.0f
                }

                view.findViewById<View>(R.id.item_swipe_left_indicator).alpha = if (scrollProgressPercent > 0) {
                    scrollProgressPercent
                } else {
                    0.0f
                }
            }
        })

        flingContainer.setOnItemClickListener { p0, p1 ->
            val view = flingContainer.selectedView
            view.findViewById<FrameLayout>(R.id.background).alpha = 0.0f
            cardAdapter.notifyDataSetChanged()
        }
    }
}

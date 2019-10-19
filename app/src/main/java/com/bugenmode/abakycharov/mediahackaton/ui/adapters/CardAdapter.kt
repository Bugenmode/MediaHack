package com.bugenmode.abakycharov.mediahackaton.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bugenmode.abakycharov.mediahackaton.R
import com.bugenmode.abakycharov.mediahackaton.data.local.model.CardModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_cards.view.*

class CardAdapter(private val context: Context, private val data: List<CardModel>) : BaseAdapter() {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var rowView = convertView

        if (rowView == null) {
            val inflater = LayoutInflater.from(context)
            rowView = inflater.inflate(R.layout.item_cards, parent, false)

            rowView.bookText.text = data[position].description

            Glide.with(context).load(data[position].imgPath).into(rowView.cardImage)

        }

        return rowView
    }

    override fun getItem(position: Int): Any = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = data.size
}
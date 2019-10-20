package com.bugenmode.abakycharov.mediahackaton.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bugenmode.abakycharov.mediahackaton.R
import com.bugenmode.abakycharov.mediahackaton.data.local.model.CardModel
import com.bugenmode.abakycharov.mediahackaton.data.remote.model.Articles
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_cards.view.*

class CardAdapter(private val context: Context, private val data: MutableList<Articles>, var listener : OnItemClickListener) : BaseAdapter() {

    private var list: MutableList<CardModel> = mutableListOf()

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var rowView = convertView

        if (rowView == null) {
            val inflater = LayoutInflater.from(context)
            rowView = inflater.inflate(R.layout.item_cards, parent, false)

            rowView.bookText.text = data[position].short_txt

            Glide.with(context).load(data[position].img_url).into(rowView.cardImage)

            rowView.btnRead.setOnClickListener {
                data[position].link_url?.let { it1 -> listener.OnItemClick(it1) }
            }
        }

        return rowView
    }

    override fun getItem(position: Int): Any = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = data.size

    fun setList(list: MutableList<CardModel>) {
        this.list = list
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun OnItemClick(url: String)
    }
}
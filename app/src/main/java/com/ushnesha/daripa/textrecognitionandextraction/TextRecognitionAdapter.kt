package com.ushnesha.daripa.textrecognitionandextraction

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_text_recognition.view.*

class TextRecognitionAdapter(private val context : Context, val textRecognitionModels : List<TextRecognitionModel>) : RecyclerView.Adapter<TextRecognitionAdapter.TextRecognitionViewHolder>(){



    class TextRecognitionViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val text1 = itemView.item_text_recognition_text_view1
        val text2 = itemView.item_text_recognition_text_view2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextRecognitionViewHolder =
                     TextRecognitionViewHolder(LayoutInflater.from(context)
                         .inflate(R.layout.item_text_recognition, parent, false))

    override fun getItemCount(): Int = textRecognitionModels.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TextRecognitionViewHolder, position: Int) {
        holder.text1.text = textRecognitionModels[position].id.toString()
        holder.text2.text = textRecognitionModels[position].text
    }


}
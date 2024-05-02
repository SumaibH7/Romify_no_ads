package com.bluell.roomdecoration.interiordesign.ui.localization

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.Languagemodel
import com.bluell.roomdecoration.interiordesign.databinding.LanguagesItemBinding

class LocalizationAdapter(
    private val languagemodelArrayList: ArrayList<Languagemodel>,
    private var selectedLanguagePosition: Int,
    private val clickListener: OnLanguageClickListener
) : RecyclerView.Adapter<LocalizationAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LanguagesItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val language = languagemodelArrayList[position]
        holder.bindData(language)
        holder.itemView.isSelected = position == selectedLanguagePosition
        holder.itemView.setOnClickListener {
            val previousSelectedItemPosition = selectedLanguagePosition
            selectedLanguagePosition = holder.adapterPosition
            notifyDataSetChanged()
            if (previousSelectedItemPosition != 6) {
                notifyItemChanged(previousSelectedItemPosition)
            }
            clickListener.onLanguageClick(language, position)
        }
    }

    override fun getItemCount(): Int {
        return languagemodelArrayList.size
    }

    inner class ViewHolder(val binding:LanguagesItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val isSelected = false

        fun bindData(item: Languagemodel) {
            binding.name.text = item.lan_name
            binding.flag.setImageResource(item.flag)
            if (adapterPosition == selectedLanguagePosition) {
                binding.select.setImageResource(R.drawable.lan_selected)
            } else {
                binding.select.setImageResource(R.drawable.lan_unselected)
            }
        }

    }

    interface OnLanguageClickListener {
        fun onLanguageClick(language: Languagemodel?, position: Int)
    }
}
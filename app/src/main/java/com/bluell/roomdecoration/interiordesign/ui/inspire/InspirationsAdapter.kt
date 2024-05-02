package com.bluell.roomdecoration.interiordesign.ui.inspire

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.DummyModelInspirations
import com.bluell.roomdecoration.interiordesign.databinding.ListItemInspirationBinding


class InspirationsAdapter(private val inspirations:ArrayList<DummyModelInspirations>,
                          private var selectedInsp: SelectedInspirations
): RecyclerView.Adapter<InspirationsAdapter.ViewHolder>() {
    class ViewHolder(val binding: ListItemInspirationBinding):RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemInspirationBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return inspirations.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = inspirations[position]

        with(holder){
            Glide.with(holder.itemView.context).load(model.image).into(binding.styleImage)
            binding.titleText.text = model.prompt
        }

        holder.itemView.setOnClickListener {
            selectedInsp.inspirations(model,position)
        }
    }

    interface SelectedInspirations {
        fun inspirations(item: DummyModelInspirations, position:Int)
    }
}
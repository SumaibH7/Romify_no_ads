package com.bluell.roomdecoration.interiordesign.ui.favorites

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.databinding.FavHistoryListItemBinding
import com.bluell.roomdecoration.interiordesign.domain.repo.MyInteriorClicks

class FavHistoryAdapter (private val itmes:MutableList<genericResponseModel>, private val clickListner: MyInteriorClicks): RecyclerView.Adapter<FavHistoryAdapter.Viewholder>() {
    inner class Viewholder(val binding: FavHistoryListItemBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = FavHistoryListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Viewholder(binding)
    }

    override fun getItemCount(): Int {
        return itmes.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {

        val item = itmes[position]
        val context = holder.itemView.context
        with(holder){

            var prompt = item.meta.prompt
            val words = prompt.split(" in ")
            if (words.size >= 2){
                val roomStyleTitle = words[1]

                binding.titleText.text = roomStyleTitle
            }
            Glide.with(context!!)
                .asBitmap()
                .load(item.output!![0])
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        binding.styleImage.scaleType= Constants.getScaleType(resource.height,resource.width,binding.styleImage.height,binding.styleImage.width)
                        binding.styleImage.setImageBitmap(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
//                        binding.imvMyCreations.setImageDrawable(placeholder)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
//                        binding.imvMyCreations.setImageDrawable(errorDrawable)
                    }

                    override fun onLoadStarted(placeholder: Drawable?) {
//                        binding.imvMyCreations.setImageDrawable(placeholder)
                    }
                })

            binding.removeFav.setOnClickListener {
                clickListner.onMenuItemClick(item,position,binding.removeFav)
            }

            holder.itemView.setOnClickListener {
                clickListner.onItemClick(item,position)
            }
        }
    }


}
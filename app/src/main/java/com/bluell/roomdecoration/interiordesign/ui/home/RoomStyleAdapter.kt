package com.bluell.roomdecoration.interiordesign.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.interfaces.RoomTypeSelectionInterface
import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.RoomTypeModel
import com.bluell.roomdecoration.interiordesign.databinding.RoomStyleItemBinding

class RoomStyleAdapter (private val roomTypes:ArrayList<RoomTypeModel>,
                        private var selectedItemPosition: Int,
                        private var roomTypeSelectionInterface: RoomTypeSelectionInterface
): RecyclerView.Adapter<RoomStyleAdapter.Viewholder>() {

    inner class Viewholder(val binding: RoomStyleItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = RoomStyleItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Viewholder(binding)
    }

    override fun getItemCount(): Int {
        return roomTypes.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {

        val rooms = roomTypes[position]
        val context = holder.itemView.context
        with(holder){
            binding.titleText.text = rooms.room_title
            binding.styleImage.setImageResource(rooms.icon)
            holder.itemView.isSelected = position == selectedItemPosition

            holder.itemView.setOnClickListener {
                roomTypeSelectionInterface.selectedRoomType(rooms, position)
            }
            if (rooms.isSelected) {
                binding.parentCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.image_color_filter))
                binding.parentCard.strokeColor = ContextCompat.getColor(context, R.color.image_color_filter)
                binding.titleText.setTextColor(ContextCompat.getColor(context, R.color.white))
                val theme = R.style.room_style_selected
                val themedContext = ContextThemeWrapper(context, theme)
                val styledAttributes = themedContext.obtainStyledAttributes(null, intArrayOf(android.R.attr.src), 0, 0)
                val drawableResId = styledAttributes.getResourceId(0, 0)
                styledAttributes.recycle()
                binding.select.setImageResource(drawableResId)
            } else {
                binding.parentCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.room_type_background))
                binding.parentCard.strokeColor = ContextCompat.getColor(context, R.color.card_stroke)
                binding.titleText.setTextColor(ContextCompat.getColor(context, R.color.room_text_color))
                val theme = R.style.room_style_unselected
                val themedContext = ContextThemeWrapper(context, theme)
                val styledAttributes = themedContext.obtainStyledAttributes(null, intArrayOf(android.R.attr.src), 0, 0)
                val drawableResId = styledAttributes.getResourceId(0, 0)
                styledAttributes.recycle()
                binding.select.setImageResource(drawableResId)
            }
        }
    }

    fun updateSelection(roomTypesNew:ArrayList<RoomTypeModel>){
        roomTypes.clear()
        roomTypes.addAll(roomTypesNew)
        notifyDataSetChanged()
    }

}
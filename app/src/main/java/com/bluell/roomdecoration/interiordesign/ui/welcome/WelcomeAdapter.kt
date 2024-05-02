package com.bluell.roomdecoration.interiordesign.ui.welcome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.databinding.WelcomeScreenItemBinding



class WelcomeAdapter(welcomeItems: List<OnboardingModel>, private val clickListener: OnGetStartedClickListner) :
    RecyclerView.Adapter<WelcomeAdapter.OnboardingViewHolder>() {
    private val welcomeItems: List<OnboardingModel>

    init {
        this.welcomeItems = welcomeItems
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = WelcomeScreenItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val item = welcomeItems[position]
        with(holder){
            binding.welcomeTitle.text = item.title
            binding.description.text = item.description
            holder.binding.getStarted.setOnClickListener {
                clickListener.onGetStarted()
            }
        }

        when (position) {
            0 -> {
                holder.binding.image.setImageResource(R.drawable.welcome_1)
                holder.binding.getStarted.visibility = View.GONE

            }
            1 -> {
                holder.binding.image.setImageResource(R.drawable.welcome_2)
                holder.binding.getStarted.visibility = View.GONE

            }
            2 -> {
                holder.binding.image.setImageResource(R.drawable.welcome_3)
                holder.binding.getStarted.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return welcomeItems.size
    }

    inner class OnboardingViewHolder(val binding:WelcomeScreenItemBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnGetStartedClickListner {
        fun onGetStarted()
    }
}
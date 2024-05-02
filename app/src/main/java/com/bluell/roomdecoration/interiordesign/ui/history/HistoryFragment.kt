package com.bluell.roomdecoration.interiordesign.ui.history

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.databinding.DialogConfirmDeletionBinding
import com.bluell.roomdecoration.interiordesign.databinding.FragmentHistoryBinding
import com.bluell.roomdecoration.interiordesign.ui.favorites.edit.EditFavoritesFragment
import com.bluell.roomdecoration.interiordesign.ui.favorites.exterior.ExteriorFavoritesFragment
import com.bluell.roomdecoration.interiordesign.ui.favorites.inspire.InspireFavoritesFragment
import com.bluell.roomdecoration.interiordesign.ui.favorites.interior.InteriorFavoritesFragment
import com.bluell.roomdecoration.interiordesign.ui.history.exterior.ExteriorHistoryFragment
import com.bluell.roomdecoration.interiordesign.ui.history.fusion.EditHistoryFragment
import com.bluell.roomdecoration.interiordesign.ui.history.inspire.InspireHistoryFragment
import com.bluell.roomdecoration.interiordesign.ui.history.interior.InteriorHistoryFragment
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()

    private var titles = arrayOf("")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titles = arrayOf(requireContext().getString(R.string.interior), requireContext().getString(R.string.exterior), requireContext().getString(R.string.inspire), requireContext().getString(R.string.edit))
        setEvents()

        setViewPager()
        initTabs()
    }


    private fun setViewPager() {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(InteriorHistoryFragment(), "Interior")
        adapter.addFragment(ExteriorHistoryFragment(), "Exterior")
        adapter.addFragment(InspireHistoryFragment(), "Inspire")
        adapter.addFragment(EditHistoryFragment(), "Edit")

        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 1
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun initTabs() {
        binding.tabLayout.setSelectedTabIndicatorHeight(0)
        val tabCount: Int = binding.tabLayout.tabCount
        for (i in 0 until tabCount) {
            val tab: TabLayout.Tab? = binding.tabLayout.getTabAt(i)
            if (tab != null) {
                tab.setCustomView(R.layout.tab_item)
                val tabCardView = tab.customView!!.findViewById<CardView>(R.id.container)
                val tabtitle = tab.customView!!.findViewById<TextView>(R.id.text)

                tabtitle.text = titles[i]



                if (i == 0) {
                    tabCardView.setCardBackgroundColor(resources.getColor(R.color.image_color_filter))
                    tabtitle.setTextColor(resources.getColor(R.color.active_tab_text_color))
                }


            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateTabAppearance(tab!!, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                updateTabAppearance(tab!!, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }

    private fun updateTabAppearance(tab: TabLayout.Tab, isSelected: Boolean) {
        val tabCardView = tab.customView!!.findViewById<CardView>(R.id.container)
        val tabTitle = tab.customView!!.findViewById<TextView>(R.id.text)
        if (tabCardView != null) {
            if (isSelected) {
                // Set the stroke color for selected tab
                tabCardView.setCardBackgroundColor(resources.getColor(R.color.image_color_filter))
                tabTitle.setTextColor(resources.getColor(R.color.active_tab_text_color))

            } else {
                // Set the stroke color for unselected tab
                tabCardView.setCardBackgroundColor(resources.getColor(R.color.bottom_nav_active_indi))
                tabTitle.setTextColor(resources.getColor(R.color.tabs_text_color))

            }
        }
    }


    fun setEvents() {

        binding.deleteAll.setOnClickListener {
            showConfirmationDialog()
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val bindingDialog = DialogConfirmDeletionBinding.inflate(layoutInflater)
        builder.setView(bindingDialog.root)
        val alertDialog = builder.create()

        alertDialog.setCancelable(false)

        bindingDialog.cancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        bindingDialog.confirmBtn.setOnClickListener {
            alertDialog.dismiss()
            when (binding.viewPager.currentItem) {
                0 -> {
                    viewModel.deleteAll(0, Constants.INTERIOR_END_POINT)
                }

                1 -> {
                    viewModel.deleteAll(0, Constants.EXTERIOR_END_POINT)
                }

                2 -> {
                    viewModel.deleteAll(0, Constants.INSPIRE_END_POINT)
                }

                3 -> {
                    viewModel.deleteAll(0, Constants.EDIT_END_POINT)
                }
            }

        }

        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
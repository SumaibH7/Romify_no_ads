package com.bluell.roomdecoration.interiordesign.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.data.local.AppDatabase
import com.bluell.roomdecoration.interiordesign.databinding.FragmentFavoritesBinding
import com.bluell.roomdecoration.interiordesign.ui.favorites.edit.EditFavoritesFragment
import com.bluell.roomdecoration.interiordesign.ui.favorites.exterior.ExteriorFavoritesFragment
import com.bluell.roomdecoration.interiordesign.ui.favorites.inspire.InspireFavoritesFragment
import com.bluell.roomdecoration.interiordesign.ui.favorites.interior.InteriorFavoritesFragment
import com.bluell.roomdecoration.interiordesign.ui.history.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var appDatabase: AppDatabase

    private var _navController: NavController? = null

    private var titles = arrayOf("")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        titles = arrayOf(
            requireContext().getString(R.string.interior),
            requireContext().getString(R.string.exterior),
            requireContext().getString(R.string.inspire),
            requireContext().getString(R.string.edit)
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _navController = findNavController()

        setEvents()

        setViewPager()
        initTabs()
    }

    private fun setViewPager() {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(InteriorFavoritesFragment(), "Interior")
        adapter.addFragment(ExteriorFavoritesFragment(), "Exterior")
        adapter.addFragment(InspireFavoritesFragment(), "Inspire")
        adapter.addFragment(EditFavoritesFragment(), "Edit")

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
                val tabTitle = tab.customView!!.findViewById<TextView>(R.id.text)

                tabTitle.text = titles[i]

                if (i == 0) {
                    tabCardView.setCardBackgroundColor(resources.getColor(R.color.image_color_filter))
                    tabTitle.setTextColor(resources.getColor(R.color.active_tab_text_color))
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
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
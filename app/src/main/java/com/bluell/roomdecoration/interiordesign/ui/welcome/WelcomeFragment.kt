package com.bluell.roomdecoration.interiordesign.ui.welcome

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.datastore.PreferenceDataStoreKeysConstants
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper
import com.bluell.roomdecoration.interiordesign.databinding.FragmentWelcomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WelcomeFragment : Fragment() {
    private var _binding:FragmentWelcomeBinding ?= null
    private val binding get() = _binding!!
    lateinit var welcomeAdapter: WelcomeAdapter

    lateinit var preferenceDataStoreHelper: UserPreferencesDataStoreHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater,container,false)
        preferenceDataStoreHelper = UserPreferencesDataStoreHelper(requireContext())
        populateOnbaordingItems()
        binding.onboardingViewPager.adapter = welcomeAdapter

        setIndicator()
        setCurrentIndicator(0)

        binding.onboardingViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)

                if (position == 2){
                    binding.skipBtn.visibility = View.INVISIBLE
                    binding.nextBtn.visibility = View.INVISIBLE
                }else{
                    binding.skipBtn.visibility = View.VISIBLE
                    binding.nextBtn.visibility = View.VISIBLE
                }
            }
        })

        binding.nextBtn.setOnClickListener {
            if (binding.onboardingViewPager.currentItem < (binding.onboardingViewPager.adapter?.itemCount
                    ?: (0 - 1))
            ) {
                binding.onboardingViewPager.setCurrentItem(binding.onboardingViewPager.currentItem + 1, true)
                lifecycleScope.launch {
                    preferenceDataStoreHelper.putPreference(
                        PreferenceDataStoreKeysConstants.ONBOARDING_COMPLETED,
                        true
                    )
                }
            }
        }

        binding.skipBtn.setOnClickListener {
            findNavController().navigate(R.id.interiorGenerationFragment)
            GlobalScope.launch {
                preferenceDataStoreHelper.putPreference(
                    PreferenceDataStoreKeysConstants.ONBOARDING_COMPLETED,
                    true
                )
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun populateOnbaordingItems() {
        val welcomeItems: MutableList<OnboardingModel> = ArrayList<OnboardingModel>()
        welcomeItems.add(OnboardingModel(R.drawable.welcome_1, requireContext().getString(R.string.customize_style), requireContext().getString(R.string.choose_design_preferences)))
        welcomeItems.add(OnboardingModel(R.drawable.welcome_2, requireContext().getString(R.string.capture_your_space), requireContext().getString(R.string.use_camera)))
        welcomeItems.add(OnboardingModel(R.drawable.welcome_3, requireContext().getString(R.string.ai_powered_designs), requireContext().getString(R.string.ai_analysis)))

        welcomeAdapter = WelcomeAdapter(welcomeItems,object:
            WelcomeAdapter.OnGetStartedClickListner {
            override fun onGetStarted() {
                findNavController().navigate(R.id.interiorGenerationFragment)

                lifecycleScope.launch {
                    preferenceDataStoreHelper.putPreference(
                        PreferenceDataStoreKeysConstants.ONBOARDING_COMPLETED,
                        true
                    )
                }


            }
        })
    }

    private fun setIndicator() {
        val welcomeIndicators = arrayOfNulls<ImageView>(welcomeAdapter.itemCount)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in welcomeIndicators.indices) {
            welcomeIndicators[i] = ImageView(requireContext())
            welcomeIndicators[i]!!.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.indicator
                )
            )
            welcomeIndicators[i]!!.layoutParams = layoutParams
            binding.layoutOnboardingIndicators.addView(welcomeIndicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.layoutOnboardingIndicators.childCount
        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        for (i in 0 until childCount) {
            val imageView = binding.layoutOnboardingIndicators.getChildAt(i) as ImageView

            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.indicator_active
                    )
                )

                if (isDarkMode) {
                    imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.indicator_dark))
                } else {
                    imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.onboarding_dark_text))
                }
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.indicator
                    )
                )

                if (isDarkMode) {
                    imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_p_color))
                } else {
                    imageView.setColorFilter(Color.parseColor("#C4C4C4"))
                }
            }
        }
    }
}
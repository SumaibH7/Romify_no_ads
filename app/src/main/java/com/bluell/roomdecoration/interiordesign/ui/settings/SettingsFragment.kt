package com.bluell.roomdecoration.interiordesign.ui.settings

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bluell.roomdecoration.interiordesign.MainActivityViewModel
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.Extras
import com.bluell.roomdecoration.interiordesign.common.datastore.PreferenceDataStoreKeysConstants
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper
import com.bluell.roomdecoration.interiordesign.databinding.FragmentSettingsBinding
import com.bluell.roomdecoration.interiordesign.databinding.RewardDialogBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max


class SettingsFragment : Fragment() {
    private var _binding:FragmentSettingsBinding ?= null
    private val binding get() = _binding!!
    private val mainActivityViewModel: MainActivityViewModel by viewModels({ requireActivity() })

    lateinit var preferenceDataStoreHelper: UserPreferencesDataStoreHelper

    private var is_dark = false

    private var isSwitchListenerEnabled = true
    private var isObserverEnabled = true

    private var counter = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater,container,false)

        val res = resources
        val src = BitmapFactory.decodeResource(res, R.drawable.welcome_1)
        val dr = RoundedBitmapDrawableFactory.create(res, src)
        dr.cornerRadius = max(src.width, src.height) / 2.0f
        dr.isCircular = true
        binding.image.setImageDrawable(dr)
        preferenceDataStoreHelper = UserPreferencesDataStoreHelper(requireContext())
        Log.e("TAG", "onCreateView: " )
        setEvents()

        initObservers()
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun initObservers(){
        mainActivityViewModel.isDark.observe(viewLifecycleOwner) {
            if (isObserverEnabled) {
                if (it != is_dark) {
                    Log.e("TAG", "initObservers: $it")
                    Log.e("TAG", "initObservers: $counter")

                    lifecycleScope.launch {
                        delay(500)
                    }
                    is_dark = it
                    isSwitchListenerEnabled = false // Disable the listener
                    binding.mySwitch.isChecked = is_dark
                    isSwitchListenerEnabled = true // Enable the listener
                }
            }
        }
    }

    fun setEvents(){
        binding.editProfile.setOnClickListener {
            if (binding.editProfileExpanded.isVisible){
                binding.editProfileExpanded.visibility = View.GONE
                binding.profileExpand.setImageResource(R.drawable.expand)

            }else{
                binding.profileExpand.setImageResource(R.drawable.expanded)
                binding.editProfileExpanded.visibility = View.VISIBLE
            }
        }

        binding.displayPic.setOnClickListener {
           openFileNameDialog()
        }

        binding.language.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_languageFragment)
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

//        binding.goPro.setOnClickListener {
//                Snackbar.make(
//                    requireActivity().findViewById(android.R.id.content),
//                    "Coming Soon!",
//                    Snackbar.LENGTH_LONG
//                )
//                    .show()
////            findNavController().navigate(R.id.action_settingsFragment_to_premiumFragment)
//        }

        binding.history.setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }


        binding.favorites.setOnClickListener {
            findNavController().navigate(R.id.favoritesFragment)
        }
        binding.terms.setOnClickListener {
            Extras.openPrivacyPolicyInBrowser(requireContext())
        }

        binding.share.setOnClickListener {
            Extras.shareApp(requireContext())
        }

        binding.rateUs.setOnClickListener {
            Extras.rateUs(requireContext())
        }

        binding.mySwitch.setOnClickListener {
            if (isSwitchListenerEnabled) {
                isObserverEnabled = false
                val isDarkMode = binding.mySwitch.isChecked // Access switch state from click event
                lifecycleScope.launch {
                    preferenceDataStoreHelper.putPreference(
                        PreferenceDataStoreKeysConstants.IS_DARK,
                        isDarkMode
                    )
                }

                isObserverEnabled = true
            }
        }
    }

    private fun openFileNameDialog(){
        val builder = AlertDialog.Builder(requireContext())
        val binding = RewardDialogBinding.inflate(layoutInflater)
        builder.setView(binding.root)
        val alertDialog = builder.create()

        alertDialog.setCancelable(true)

        Glide.with(this)
            .asGif() // Specify that you're loading a GIF
            .load(R.raw.earned_reward_anim) // Replace with the resource ID of your GIF
            .transition(DrawableTransitionOptions.withCrossFade()) // Optional crossfade animation
            .into(binding.earnedAnimation)

        binding.closeDialog.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()
    }
}
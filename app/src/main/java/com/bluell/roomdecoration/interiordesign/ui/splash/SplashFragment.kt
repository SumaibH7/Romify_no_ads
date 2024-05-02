package com.bluell.roomdecoration.interiordesign.ui.splash

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bluell.roomdecoration.interiordesign.MainActivity
import com.bluell.roomdecoration.interiordesign.R
import com.bluell.roomdecoration.interiordesign.common.datastore.PreferenceDataStoreKeysConstants
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper
import com.bluell.roomdecoration.interiordesign.databinding.FragmentSplashScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!
    private lateinit var splashanim: AnimationDrawable
    lateinit var preferenceDataStoreHelper: UserPreferencesDataStoreHelper
    private var isOnBoardingCompleted = false

    private lateinit var mActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceDataStoreHelper = UserPreferencesDataStoreHelper(requireContext())
        initObserver()

        Log.d("TAG12345", "onViewCreated: 323232")

        binding.splashAnimation.apply {
            setBackgroundResource(R.drawable.splash_animation)
            splashanim = background as AnimationDrawable
            splashanim.start()
        }

        navigateToNextScreen()

    }

    private fun navigateToNextScreen() {
        if (isAdded) {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(3000)
                if (isOnBoardingCompleted) {
                    findNavController().navigate(R.id.interiorGenerationFragment)
                } else {
                    findNavController().navigate(R.id.welcomeFragment)
                }
            }
        } else {
            Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initObserver() {
        lifecycleScope.launch {
            preferenceDataStoreHelper.getPreference(
                PreferenceDataStoreKeysConstants.ONBOARDING_COMPLETED,
                false
            ).collect() {
                isOnBoardingCompleted = it
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
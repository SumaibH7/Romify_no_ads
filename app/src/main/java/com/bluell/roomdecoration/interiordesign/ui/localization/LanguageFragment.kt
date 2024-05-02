package com.bluell.roomdecoration.interiordesign.ui.localization

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluell.roomdecoration.interiordesign.MainActivity
import com.bluell.roomdecoration.interiordesign.common.Constants
import com.bluell.roomdecoration.interiordesign.common.Constants.TAG
import com.bluell.roomdecoration.interiordesign.common.LocaleManager
import com.bluell.roomdecoration.interiordesign.common.datastore.PreferenceDataStoreKeysConstants
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper
import com.bluell.roomdecoration.interiordesign.data.models.dummymodels.Languagemodel
import com.bluell.roomdecoration.interiordesign.databinding.FragmentLanguageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class LanguageFragment : Fragment() {
    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!
    private lateinit var languageViewModel: LanguageViewModel

    var selectedItem: Languagemodel? = null

    private var sel: String = "en"
    var selected = -1

    private var pos = 0
    var adapter: LocalizationAdapter? = null

    private var _navController: NavController? = null
    private val navController get() = _navController!!
    lateinit var preferenceDataStoreHelper: UserPreferencesDataStoreHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        preferenceDataStoreHelper = UserPreferencesDataStoreHelper(requireContext())
        _navController = findNavController()

        languageViewModel = ViewModelProvider(requireActivity())[LanguageViewModel::class.java]

        getFromDataStore()
        setEvents()
        return binding.root
    }

    private fun setEvents() {
        binding.backBtn.setOnClickListener {
            navController.navigateUp()
        }
        binding.selectLanguage.setOnClickListener {
            GlobalScope.launch {
                preferenceDataStoreHelper.putPreference(
                    PreferenceDataStoreKeysConstants.selectLanguageCode,
                    selectedItem!!.lan_code
                )

                preferenceDataStoreHelper.putPreference(
                    PreferenceDataStoreKeysConstants.selectedLangugaePosition,
                    selected
                )
            }
            val context = LocaleManager.setLocale(requireContext(), selectedItem!!.lan_code)
            val resources = context.resources
            val newLocale = Locale(selectedItem!!.lan_code)
            val resources1 = getResources()
            val configuration = resources1.configuration
            configuration.setLocale(newLocale)
            configuration.setLayoutDirection(Locale(selectedItem!!.lan_code))
            resources1.updateConfiguration(configuration, resources.displayMetrics)
            languageViewModel.setSelectedLanguage(selectedItem!!.lan_code)
            navController.navigateUp()
        }
    }

    private fun getFromDataStore() {
        GlobalScope.launch {
            preferenceDataStoreHelper.getPreference(
                PreferenceDataStoreKeysConstants.selectLanguageCode,
                "en"
            )
                .collect() {
                    if (it.isNotEmpty()) {
                        Log.e("TAG", "observeDataStoreManager: Observed token is $it")
                        sel = it
                    }
                }
        }

        GlobalScope.launch {
            pos = preferenceDataStoreHelper.getPreference(
                PreferenceDataStoreKeysConstants.selectedLangugaePosition, 0
            ).first()
            Log.e("TAG", "getFromDataStore: $pos")
            selected = pos
            selectedItem = Constants.getLanguageList(sel)[selected]
            Log.d(TAG, "getFromDataStore123: "+ selectedItem!!.lan_code)
        }
        setUI()
    }

    private fun setUI() {
        GlobalScope.launch(Dispatchers.Main) {
            binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
            adapter =
                LocalizationAdapter(Constants.getLanguageList(sel), pos, object :
                    LocalizationAdapter.OnLanguageClickListener {

                    override fun onLanguageClick(language: Languagemodel?, position: Int) {
                        selectedItem = language
                        selected = position
                    }
                })
            binding.rvLanguages.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
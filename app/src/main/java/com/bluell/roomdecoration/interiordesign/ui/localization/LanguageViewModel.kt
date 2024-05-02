package com.bluell.roomdecoration.interiordesign.ui.localization

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LanguageViewModel : ViewModel() {
    private val _selectedLanguage = MutableLiveData<String>()
    val selectedLanguage: LiveData<String> = _selectedLanguage

    fun setSelectedLanguage(language: String) {
        _selectedLanguage.value = language
    }
}
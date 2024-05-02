package com.bluell.roomdecoration.interiordesign

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluell.roomdecoration.interiordesign.common.Constants.TAG
import com.bluell.roomdecoration.interiordesign.common.datastore.PreferenceDataStoreKeysConstants
import com.bluell.roomdecoration.interiordesign.common.datastore.UserPreferencesDataStoreHelper
import com.bluell.roomdecoration.interiordesign.data.models.response.newUser
import com.bluell.roomdecoration.interiordesign.domain.usecase.UpdateGemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val updateGemsUseCase: UpdateGemsUseCase, application: MyApplication) :
    ViewModel() {
    private var _create = MutableLiveData<String>()
    val create: LiveData<String> = _create

    private var _cross = MutableLiveData<String>()
    val cross: LiveData<String> = _cross

    private var _default = MutableLiveData<String>()
    val default: LiveData<String> = _default

    private var _positiveButtonClick = MutableLiveData<Boolean>()
    val positiveButtonClick: LiveData<Boolean> = _positiveButtonClick

    private var _positiveButtonClickInfo = MutableLiveData<Boolean>()
    val positiveButtonClickInfo: LiveData<Boolean> = _positiveButtonClickInfo

    private var _negativeButtonClick = MutableLiveData<Boolean>()
    val negativeButtonClick: LiveData<Boolean> = _negativeButtonClick

    private var _user = MutableLiveData<newUser>()
    val user: LiveData<newUser> = _user

//    private var _isGenerateClick =MutableLiveData<Boolean>(false)
//    val isGenerateClick:LiveData<Boolean> = _isGenerateClick

    private val _currentTheme = MutableStateFlow(Theme.LIGHT)
    val currentTheme: StateFlow<Theme> = _currentTheme

    private var _isDrak = MutableLiveData<Boolean>(false)
    val isDark: LiveData<Boolean> = _isDrak


    val preferenceDataStoreHelper = UserPreferencesDataStoreHelper(application.applicationContext)


    init {
        // Load the initial dark mode value from DataStore and cache it.
        viewModelScope.launch {
            preferenceDataStoreHelper.getPreference(
                PreferenceDataStoreKeysConstants.IS_DARK,
                false
            ).collect(){
                _isDrak.value = it
            }
             // Cache the value in memory.
        }
    }

    fun getMoodFromDataStore(){
        viewModelScope.launch {
            preferenceDataStoreHelper.getPreference(
                PreferenceDataStoreKeysConstants.IS_DARK,
                false
            ).collect(){
                _isDrak.value = it
            }
        }

    }


    fun toggleTheme() {
        _currentTheme.value = if (_currentTheme.value == Theme.LIGHT) Theme.DARK else Theme.LIGHT
    }


    fun subtractGems() {
        val user = this.user.value

        if (user?.gemsFromSub!! >= 2) {
            user.gemsFromSub = user.gemsFromSub!! - 2

        } else {
            if (user.otherGems!! >= 2){
                user.otherGems = user.otherGems!!.minus(2)
            }else{
                user.otherGems= user.otherGems!!.minus(1)
                user.gemsFromSub= user.gemsFromSub!!.minus(1)
                user.totalGems = user.otherGems!! + user.gemsFromSub!!


            }

        }

        user.totalGems = user.otherGems!! + user.gemsFromSub!!

        this._user.value=user
        val hashMap = mapOf<String, Any>(
            ("deviceID" to user.deviceID!!),
            "purchaseToken" to user.purchaseToken!!,
            "totalGems" to user.gemsFromSub!! + user.otherGems!!,
            "usedGems" to user.usedGems!!,
            "updateDate" to user.updateDate.toString(),
            "purchaseType" to user.type!!,
            "gemsFromSubs" to user.gemsFromSub!!,
            "otherGems" to user.otherGems!!,
            "subExpiryDate" to user.expiry!!
        )

        viewModelScope.launch {
            updateGemsUseCase.invoke(hashMap).collect() {
                Log.e(TAG, "updateTotalGems: $it")
            }
        }
    }

    fun updateTotalGems(user: newUser) {
        val hashMap = mapOf<String, Any>(
            "deviceID" to user.deviceID!!,
            "purchaseToken" to user.purchaseToken!!,
            "totalGems" to user.totalGems!!,
            "usedGems" to user.usedGems!!,
            "updateDate" to System.currentTimeMillis().toString()
        )
        viewModelScope.launch {
            updateGemsUseCase.invoke(hashMap).collect() {
                Log.e(TAG, "updateTotalGems: $it")
            }
        }
    }


    fun updateInAPPGems(newUser: newUser) {
        val hashMap = mapOf<String, Any>(
            "deviceID" to newUser.deviceID!!,
            ("purchaseToken" to newUser.purchaseToken!!),
            "totalGems" to newUser.gemsFromSub!! + newUser.otherGems!!,
            "usedGems" to newUser.usedGems!!,
            "updateDate" to System.currentTimeMillis().toString(),
            "purchaseType" to newUser.type!!,
            "gemsFromSubs" to newUser.gemsFromSub!!,
            "otherGems" to newUser.otherGems!!,
            "subExpiryDate" to newUser.expiry!!
        )
        Log.e(TAG, "updateInAPPGems: $newUser")
        viewModelScope.launch {
            updateGemsUseCase.invoke(hashMap).collect() {
                Log.e(TAG, "updateTotalGems: $it")
            }
        }
    }
//
//    fun setButtonGenerateClick(generateClick:Boolean=true){
//        _isGenerateClick.value=generateClick
//    }
//
//    fun clearButtonClick(){
//        _isGenerateClick.value=false
//    }
    fun setUser(user: newUser) {
        _user.value = user
    }

    fun setCreate() {
        _create.value = "creating"
    }

    fun setCross() {
        _cross.value = "crossing"
    }

    fun setDefault() {
        _default.value = "default"
    }

    fun setPositiveClick() {
        _positiveButtonClick.value = true
    }

    fun setPositiveClickINfo() {
        _positiveButtonClickInfo.value = true
    }

    fun setNegativeClick() {
        _negativeButtonClick.value = true
    }




}

enum class Theme {
    LIGHT, DARK
}
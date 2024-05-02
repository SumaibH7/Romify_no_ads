package com.bluell.roomdecoration.interiordesign.common.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceDataStoreKeysConstants {
    val FIREBASE_TOKEN = stringPreferencesKey("FIREBASE_TOKEN")
    val WORK_MANAGER_JOB_GENERIC = stringPreferencesKey("WORK_MANAGER_JOB_GENERIC")
    val selectLanguageCode = stringPreferencesKey("selectedItemCode")
    val selectedLangugaePosition = intPreferencesKey("position")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("isOnboardingComplete")
    val IS_DARK = booleanPreferencesKey("IS_DARK")
    val LAST_CLAIMED_TIME = longPreferencesKey("LAST_CLAIMED_TIME")
}
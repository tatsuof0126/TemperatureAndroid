package com.tatsuo.bodytemperature

import android.preference.PreferenceManager

class ConfigManager {

    val KEY_UPDATED_DATA_FLAG = "UPDATED_DATA_FLAG"
    val KEY_SHOW_ADS_FLAG = "SHOW_ADS_FLAG"
    val KEY_GRAPH_TYPE = "GRAPH_TYPE"
    val KEY_TARGET_PERSON_ID = "TARGET_PERSON_ID"
    val KEY_TARGET_PERSON_NAME = "TARGET_PERSON_NAME"

    fun loadUpdatedDataFlag() : Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(TemperatureApplication.instance)
        return preferences.getBoolean(KEY_UPDATED_DATA_FLAG, false)
    }
    
    fun saveUpdatedDataFlag(updatedDataFlag : Boolean) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(TemperatureApplication.instance)
        val editor = preferences.edit()
        editor.putBoolean(KEY_UPDATED_DATA_FLAG, updatedDataFlag)
        editor.apply()
    }

    fun loadShowAdsFlag() : Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(TemperatureApplication.instance)
        return preferences.getBoolean(KEY_SHOW_ADS_FLAG, true)
    }

    fun saveShowAdsFlag(showAdsFlag : Boolean) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(TemperatureApplication.instance)
        val editor = preferences.edit()
        editor.putBoolean(KEY_SHOW_ADS_FLAG, showAdsFlag)
        editor.apply()
    }

    fun loadGraphType() : Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(TemperatureApplication.instance)
        return preferences.getInt(KEY_GRAPH_TYPE, 1)
    }

    fun saveGraphType(graphType : Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(TemperatureApplication.instance)
        val editor = preferences.edit()
        editor.putInt(KEY_GRAPH_TYPE, graphType)
        editor.apply()
    }

    fun loadTargetPersonId() : Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(TemperatureApplication.instance)
        return preferences.getInt(KEY_TARGET_PERSON_ID, 0)
    }

    fun saveTargetPersonId(targetPersonId : Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(TemperatureApplication.instance)
        val editor = preferences.edit()
        editor.putInt(KEY_TARGET_PERSON_ID, targetPersonId)
        editor.apply()
    }

    fun loadTargetPersonName() : String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(TemperatureApplication.instance)
        return preferences.getString(KEY_TARGET_PERSON_NAME, "")
    }

    fun saveTargetPersonName(targetPersonName : String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(TemperatureApplication.instance)
        val editor = preferences.edit()
        editor.putString(KEY_TARGET_PERSON_NAME, targetPersonName)
        editor.apply()
    }

}

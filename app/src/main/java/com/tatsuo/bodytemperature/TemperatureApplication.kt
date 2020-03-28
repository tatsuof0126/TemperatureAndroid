package com.tatsuo.bodytemperature

import android.app.Application
import androidx.multidex.MultiDexApplication

class TemperatureApplication : MultiDexApplication() {

    companion object {
        lateinit var instance: Application private set
        var dbUpdating = false
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}

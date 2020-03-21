package com.tatsuo.temperature

import android.app.Application
import androidx.multidex.MultiDexApplication

class TemperatureApplication : MultiDexApplication() {

    companion object {
        lateinit var instance: Application private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}

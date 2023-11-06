package com.joinself.sdk.sample

import android.app.Application
import com.joinself.sdk.SelfSDK

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        SelfSDK.initialize(applicationContext)
    }

    override fun onTerminate() {
        super.onTerminate()

        SelfSDK.close(applicationContext)
    }
}
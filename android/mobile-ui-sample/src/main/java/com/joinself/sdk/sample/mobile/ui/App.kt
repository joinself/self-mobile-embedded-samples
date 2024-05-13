package com.joinself.sdk.sample.mobile.ui

import android.app.Application
import com.joinself.sdk.SelfSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        setupTimberLog()
        SelfSDK.initialize(applicationContext)
    }

    override fun onTerminate() {
        super.onTerminate()

        SelfSDK.close(applicationContext)
    }

    private fun setupTimberLog() {
        CoroutineScope(Dispatchers.Default).launch {
            if (Timber.treeCount == 0 && BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }
        }
    }
}
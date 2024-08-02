package com.joinself.sdk.sample.chat.compose

import android.app.Application
import android.util.Log
import com.joinself.sdk.SelfSDK
import fr.bipi.tressence.file.FileLoggerTree
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

            val fileTree =  FileLoggerTree.Builder()
                .withFileName("file%g.log")
                .withDir(applicationContext.cacheDir)
                .withSizeLimit(5000000)
                .withFileLimit(1)
                .withMinPriority(Log.DEBUG)
                .appendToFile(true)
                .buildQuietly()
            Timber.plant(fileTree)
        }
    }
}
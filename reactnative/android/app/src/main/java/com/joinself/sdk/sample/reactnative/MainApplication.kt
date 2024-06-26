package com.joinself.sdk.sample.reactnative

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.react.flipper.ReactNativeFlipper
import com.facebook.soloader.SoLoader
import com.joinself.sdk.Environment
import com.joinself.sdk.SelfSDK
import com.joinself.sdk.models.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MainApplication : Application(), ReactApplication {

    lateinit var account: Account

    override val reactNativeHost: ReactNativeHost =
        object : DefaultReactNativeHost(this) {
            override fun getPackages(): List<ReactPackage> =
                PackageList(this).packages.apply {
                    // Packages that cannot be autolinked yet can be added manually here, for example:
                    // add(MyReactNativePackage())
                    add(SelfSDKRNPackage())
                }

            override fun getJSMainModuleName(): String = "index"
            override fun getBundleAssetName(): String? {
                return "index.android.bundle"
            }

            override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG

            override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
            override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
        }

    override val reactHost: ReactHost
        get() = getDefaultReactHost(this.applicationContext, reactNativeHost)

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            // If you opted-in for the New Architecture, we load the native entry point for this app.
            load()
        }

        setupTimberLog()

        SelfSDK.initialize(applicationContext)

        account = Account.Builder()
            .setContext(applicationContext)
            .setEnvironment(Environment.review)
            .setStoragePath("account1")
            .build()

        account.setDevMode(true)
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

package com.joinself.sdk.sample.reactnative
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.bridge.Arguments
import timber.log.Timber


class SelfSDKRNModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "SelfSDKRNModule"
    }



    private var rnContext: ReactContext

    init {
        rnContext = reactContext

        instance = this
    }

    companion object {
        var instance: SelfSDKRNModule? = null
        var openLivenessCheckCallback: (()->Unit)? = null
    }


    // send event from kotlin to react native
    private fun sendEvent(reactContext: ReactContext, eventName: String, params: WritableMap?) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    fun sendSelfieEvent(result: Boolean) {
        val params = Arguments.createMap().apply {
            putBoolean("result", result)
        }
        sendEvent(rnContext, "EventSelfieVerification", params)
    }

    @ReactMethod
    fun createTestEvent(name: String, callback: Callback) {
        Timber.d("Create event called with name: $name")

        callback.invoke("hello from sdk", "abc")
    }

    @ReactMethod
    fun openLivenessCheck(callback: Callback) {
        Timber.d("openLivenessCheck")
        openLivenessCheckCallback?.invoke()
    }
}
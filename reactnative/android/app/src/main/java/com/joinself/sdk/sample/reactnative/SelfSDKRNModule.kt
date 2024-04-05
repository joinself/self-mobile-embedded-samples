package com.joinself.sdk.sample.reactnative
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.joinself.sdk.models.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber


class SelfSDKRNModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "SelfSDKRNModule"
    }

    val scope = CoroutineScope(Job())

    private var rnContext: ReactContext
    private var hasListeners = false
    init {
        rnContext = reactContext

        instance = this

        Timber.d("SelfSDKRNModule initialized")
    }

    override fun invalidate() {
        super.invalidate()
        scope.cancel()
    }

    companion object {
        var instance: SelfSDKRNModule? = null
        var account: Account? = null
        var createAccountCallback: (()->Unit)? = null
        var livenessCheckCallback: (()->Unit)? = null
        var getKeyValueCallback: ((String, (String?)->Unit)->Unit)? = null
    }

    @ReactMethod
    fun addListener(eventName: String?) {
        Timber.d("addListener $eventName")
        hasListeners = true
    }

    @ReactMethod
    fun removeListeners(count: Int?) {
        Timber.d("removeListeners $count")
        hasListeners = false
    }

    // send event from kotlin to react native
    private fun sendEvent(reactContext: ReactContext, eventName: String, params: WritableMap?) {
        if (hasListeners) {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        }
    }

    fun sendSelfId(selfId: String) {
        val params = Arguments.createMap().apply {
            putString("selfId", selfId)
        }
        sendEvent(rnContext, "EventSelfId", params)
    }

    @ReactMethod
    fun createTestEvent(name: String, callback: Callback) {
        Timber.d("Create event called with name: $name")

        callback.invoke("hello from sdk", "abc")
    }

    @ReactMethod
    fun getSelfId(callback: Callback) {
        callback.invoke(account?.identifier() ?: "")
    }

    @ReactMethod
    fun getLocation(successCallback: Callback, errorCallback: Callback) {
        if (!checkLocationPermission()) {
            errorCallback.invoke("location permission required")
            return
        }
        Timber.d("getLocation")
        scope.launch {
            val locationAttestation = account?.location()
            val value = locationAttestation?.firstOrNull()?.fact()?.value() ?: ""
            successCallback.invoke(value)
        }

    }

    @ReactMethod
    fun createAccount(callback: Callback) {
        Timber.d("createAccount")
        if (!account?.identifier().isNullOrEmpty()) return
        createAccountCallback?.invoke()
    }

    @ReactMethod
    fun livenessCheck(callback: Callback) {
        Timber.d("livenessCheck")
        livenessCheckCallback?.invoke()
    }

    private fun checkLocationPermission(): Boolean {
        return !(ActivityCompat.checkSelfPermission(reactApplicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(reactApplicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
    }

    @ReactMethod
    fun exportBackup(callback: Callback) {
        scope.launch {
            val backupFile = account?.backup()
            if (backupFile != null) {
                Timber.d("exportBackup: ${backupFile.absolutePath}")
                callback.invoke(backupFile.absolutePath)
            }
        }
    }

    @ReactMethod
    fun getKeyValue(key: String, callback: Callback) {
        Timber.d("getKeyValue key: $key")
        getKeyValueCallback?.invoke(key) { value ->
            callback.invoke(value)
        }
    }
}
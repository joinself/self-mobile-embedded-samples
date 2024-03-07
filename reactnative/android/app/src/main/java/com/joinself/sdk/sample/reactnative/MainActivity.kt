package com.joinself.sdk.sample.reactnative

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate
import com.joinself.sdk.sample.reactnative.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

//    /**
//     * Returns the name of the main component registered from JavaScript. This is used to schedule
//     * rendering of the component.
//     */
//    override fun getMainComponentName(): String = "reactnative"
//
//    /**
//     * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
//     * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
//     */
//    override fun createReactActivityDelegate(): ReactActivityDelegate =
//        DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)


    override fun onResume() {
        super.onResume()

        SelfSDKRNModule.openLivenessCheckCallback = {
            openLivenssCheck()
        }
    }

    private fun openLivenssCheck() {
        val livenessCheckFragment = LivenessCheckFragment()

    }
}

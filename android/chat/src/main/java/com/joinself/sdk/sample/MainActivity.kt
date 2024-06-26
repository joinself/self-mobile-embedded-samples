package com.joinself.sdk.sample

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.fragment.NavHostFragment
import com.joinself.sdk.sample.chat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}
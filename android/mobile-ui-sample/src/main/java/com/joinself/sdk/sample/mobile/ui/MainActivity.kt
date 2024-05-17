package com.joinself.sdk.sample.mobile.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joinself.sdk.Environment
import com.joinself.sdk.models.Account
import com.joinself.sdk.sample.mobile.ui.theme.SelfSDKSamplesTheme
import com.joinself.sdk.ui.LivenessCheckView

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup account
        val account = Account.Builder()
            .setContext(this)
            .setEnvironment(Environment.review)
            .setStoragePath("account1")
            .build()
        account.setDevMode(true)

        setContent {
            val coroutineScope = rememberCoroutineScope()
            val navController = rememberNavController()
            var selfId: String? by remember { mutableStateOf(account.identifier()) }
            var showDialog by remember { mutableStateOf(false) }

            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    SelfSDKSamplesTheme {
                        Surface(modifier = Modifier
                            .fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MainView()
                        }
                    }
                }
                composable("messaging") {
                    SelfSDKSamplesTheme {
                        Surface(modifier = Modifier
                            .fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MessagingView(account = account, onBack = {
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(0.dp)
    ) {
        TopAppBar(title = { Text(text = "Mobile UI - SDK Sample" ) },
            modifier = Modifier.padding(bottom = 16.dp))


        LivenessCheckView()

    }
}

@Composable
fun ProgressDialog(showDialog: Boolean) {
    if (showDialog) {
        Dialog(
            onDismissRequest = { },
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                contentAlignment= Alignment.Center,

                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(64.dp)
                        .height(64.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    trackColor = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SamplePreview() {
    SelfSDKSamplesTheme {

    }
}

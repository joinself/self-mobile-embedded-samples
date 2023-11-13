package com.joinself.sdk.sample.chat.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joinself.sdk.Environment
import com.joinself.sdk.models.Account
import com.joinself.sdk.sample.chat.compose.ui.theme.SelfSDKSamplesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = Account.Builder()
            .setContext(this)
            .setEnvironment(Environment.review)
            .setStoragePath("account1")
            .build()

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    SelfSDKSamplesTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 0.dp, top = 50.dp, end = 0.dp, bottom = 0.dp),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MainView(account = account,
                                onNavigateToLivenessCheck = {
                                    navController.navigate("livenessCheck")
                                }, onNavigateToMessaging = {
                                    navController.navigate("messaging")
                                })
                        }
                    }
                }
                composable("livenessCheck") {
                    SelfSDKSamplesTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(modifier = Modifier.fillMaxSize().padding(start = 0.dp, top = 50.dp, end = 0.dp, bottom = 0.dp),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            LivenessCheckView(account = account)
                        }
                    }
                }
                composable("messaging") {
                    SelfSDKSamplesTheme {
                        Surface(modifier = Modifier.fillMaxSize().padding(start = 0.dp, top = 50.dp, end = 0.dp, bottom = 0.dp),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MessagingView(account = account)
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun MainView(account: Account,
             onNavigateToLivenessCheck: () -> Unit,
             onNavigateToMessaging: () -> Unit) {
    val selfId = account.identifier()
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(10.dp)
    ) {
        Text(
            text = "SelfId: $selfId",
        )

        Button(onClick = {  }, enabled = selfId.isNullOrEmpty()) {
            Text(text = stringResource(id = R.string.button_create_account))
        }

        Button(onClick = {
            onNavigateToMessaging.invoke()
        }, enabled = !selfId.isNullOrEmpty()) {
            Text(text = "Messaging")
        }

        Button(onClick = {
            onNavigateToLivenessCheck.invoke()
        }, enabled = true) {
            Text(text = "Liveness Check")
        }
    }
}

@Composable
fun LivenessCheckView(account: Account) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(10.dp)
    ) {
        Text(
            text = "Description: "
        )
        Text(
            text = "Status: "
        )
    }
}
@Composable
fun MessagingView(account: Account) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(10.dp)
    ) {
        Text(
            text = "Description: "
        )
        Text(
            text = "Status: "
        )
    }
}
package com.joinself.sdk.sample.chat.compose

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import com.joinself.sdk.Environment
import com.joinself.sdk.liveness.LivenessCheck
import com.joinself.sdk.models.Account
import com.joinself.sdk.sample.chat.compose.ui.theme.SelfSDKSamplesTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    val livenessCheck = LivenessCheck()

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
                        Surface(modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 0.dp, top = 50.dp, end = 0.dp, bottom = 0.dp),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            LivenessCheckScreen(livenessCheck = livenessCheck, account = account, activity = this@MainActivity)
                        }
                    }
                }
                composable("messaging") {
                    SelfSDKSamplesTheme {
                        Surface(modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 0.dp, top = 50.dp, end = 0.dp, bottom = 0.dp),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MessagingView(account = account)
                        }
                    }
                }
            }

        }
    }

    override fun onPause() {
        super.onPause()

        livenessCheck.stop()
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
        }, enabled = selfId.isNullOrEmpty()) {
            Text(text = "Messaging")
        }

        Button(onClick = {
            onNavigateToLivenessCheck.invoke()
        }, enabled = true) {
            Text(text = "Liveness Check")
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LivenessCheckScreen(livenessCheck: LivenessCheck, account: Account, activity: Activity) {
    val context = LocalContext.current
    val cameraPermissionState =
        rememberPermissionState(permission = android.Manifest.permission.CAMERA)

    PermissionRequired(
        permissionState = cameraPermissionState,
        permissionNotGrantedContent = {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
        },
        permissionNotAvailableContent = {
            Column {
                Toast.makeText(context, "Permission denied.", Toast.LENGTH_LONG).show()
            }
        },
        content = {
            LivenessCheckView(livenessCheck = livenessCheck, account = account, activity = activity)
        }
    )
}

@Composable
fun LivenessCheckView(livenessCheck: LivenessCheck, account: Account, activity: Activity) {
    var cameraPreview: com.joinself.sdk.liveness.CameraSourcePreview? = null
    var graphicOverlay: com.joinself.sdk.liveness.GraphicOverlay? = null

    var challenge: LivenessCheck.Challenge by remember { mutableStateOf(LivenessCheck.Challenge.None) }
    var status: LivenessCheck.Status by remember { mutableStateOf(LivenessCheck.Status.Info) }
    var error: LivenessCheck.Error? by remember { mutableStateOf(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Text(
            text = "Challenge: ${challenge.name}"
        )
        Text(
            text = "Status: ${error?.name ?: status.name}"
        )
        Surface(modifier = Modifier
            .width(300.dp)
            .height(300.dp)) {

            AndroidView(modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    cameraPreview = com.joinself.sdk.liveness.CameraSourcePreview(ctx, null)

                    cameraPreview!!
                })
            AndroidView(modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    graphicOverlay = com.joinself.sdk.liveness.GraphicOverlay(ctx, null)

                    graphicOverlay!!
                })
        }
    }

    LaunchedEffect(Unit) {
        if (graphicOverlay != null && cameraPreview != null) {
            livenessCheck.initialize(account, activity, graphicOverlay!!, cameraPreview!!,
                onStatusUpdated = { sts ->
                    Timber.d("status: $sts")
                    status = sts
                },
                onChallengeChanged = { chgn ->
                    Timber.d("challenge: $chgn")
                    challenge = chgn
                },
                onError = { err ->
                    Timber.d("error: $err")
                    error = err
                },
                onResult = { attestation ->
                    Timber.d("attestation: $attestation")

                    livenessCheck.stop()
                })

            livenessCheck.start()
        }
    }
}

@Composable
fun MessagingView(account: Account) {
    var toSelfId by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(10.dp)
    ) {
        Row {
            Text(
                text = "To SelfId: "
            )
            TextField(
                value = toSelfId,
                onValueChange = { toSelfId = it },
                maxLines = 1, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}
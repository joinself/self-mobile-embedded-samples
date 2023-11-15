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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.joinself.sdk.models.Attestation
import com.joinself.sdk.models.ChatMessage
import com.joinself.sdk.models.Message
import com.joinself.sdk.sample.chat.compose.ui.theme.SelfSDKSamplesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val account = Account.Builder()
            .setContext(this)
            .setEnvironment(Environment.review)
            .setStoragePath("account1")
            .build()
        var attestationCallBack: ((attesation: Attestation?) -> Unit)? = null

        setContent {
            val coroutineScope = rememberCoroutineScope()
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
                                onCreateAccount = {
                                    attestationCallBack = { attestation ->
                                        if (account.identifier().isNullOrEmpty() && attestation != null) {
                                            coroutineScope.launch(Dispatchers.Default) {
                                                val selfId = account.register(selfieAttestation = attestation)
                                                Timber.d("SelfId: $selfId")
                                            }
                                        }
                                    }
                                    navController.navigate("livenessCheck")
                                },
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
                            .padding(start = 0.dp, top = 30.dp, end = 0.dp, bottom = 0.dp),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            LivenessCheckScreen(account = account, activity = this@MainActivity) { attestation ->
                                if (attestationCallBack != null) {
                                    attestationCallBack?.invoke(attestation)
                                    attestationCallBack = null
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
                composable("messaging") {
                    SelfSDKSamplesTheme {
                        Surface(modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
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

//        livenessCheck.stop()
    }
}

@Composable
fun MainView(account: Account,
             onCreateAccount: () -> Unit,
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

        Button(onClick = {
            onCreateAccount.invoke()
        }, enabled = selfId.isNullOrEmpty()) {
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


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LivenessCheckScreen(account: Account, activity: Activity, onResult: (attestation: Attestation?) -> Unit) {
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
            LivenessCheckView(account = account, activity = activity, onResult)
        }
    )
}

@Composable
fun LivenessCheckView(account: Account, activity: Activity, onResult: (attestation: Attestation?) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val livenessCheck = LivenessCheck()

    var cameraPreview: com.joinself.sdk.liveness.CameraSourcePreview? = null
    var graphicOverlay: com.joinself.sdk.liveness.GraphicOverlay? = null

    var challenge: LivenessCheck.Challenge? by remember { mutableStateOf(LivenessCheck.Challenge.None) }
    var status: LivenessCheck.Status by remember { mutableStateOf(LivenessCheck.Status.Info) }
    var error: LivenessCheck.Error? by remember { mutableStateOf(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 10.dp)
    ) {
        var txt = when (challenge) {
            LivenessCheck.Challenge.Smile -> stringResource(id = R.string.msg_liveness_smile)
            LivenessCheck.Challenge.Blink -> stringResource(R.string.msg_liveness_blink)
            LivenessCheck.Challenge.TurnLeft -> stringResource(R.string.msg_liveness_turn_left)
            LivenessCheck.Challenge.TurnRight -> stringResource(R.string.msg_liveness_turn_right)
            LivenessCheck.Challenge.Done -> stringResource(R.string.thank_you_2)
            else -> ""
        }

        if (error != null) {
            txt = when (error) {
                LivenessCheck.Error.FaceChanged -> stringResource(R.string.error_liveness_out_of_preview)
                LivenessCheck.Error.OutOfPreview -> stringResource(R.string.msg_liveness_desc)
                else -> ""
            }
        }

        Text(
            text = txt,
            textAlign = TextAlign.Center,
            maxLines = 3, minLines = 2
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

    LaunchedEffect(lifecycleOwner) {
        if (graphicOverlay != null && cameraPreview != null) {
            livenessCheck.initialize(account, activity, graphicOverlay!!, cameraPreview!!,
                onStatusUpdated = { sts ->
                    Timber.d("status: $sts")
                    status = sts
                },
                onChallengeChanged = { chgn ->
                    Timber.d("challenge: $chgn")
                    challenge = chgn
                    error = null
                },
                onError = { err ->
                    Timber.d("error: $err")
                    error = err
                },
                onResult = { attestation ->
                    Timber.d("attestation: $attestation")
                    onResult.invoke(attestation)
                })

            livenessCheck.start()
        }
    }
    DisposableEffect(lifecycleOwner) {
        onDispose {
            livenessCheck.stop()
        }
    }
}

@Composable
fun MessagingView(account: Account) {
    val coroutineScope = rememberCoroutineScope()
    var toSelfId by remember { mutableStateOf("20084590084") }
    var msgText by remember { mutableStateOf("") }

    val messages = remember { mutableStateListOf<Message>() }
    account.setOnMessageListener {
        messages.add(it)
    }
    account.setOnRequestListener {
        messages.add(it)
    }
    account.setOnResponseListener {
        messages.add(it)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
        Column {
            messages.forEach {msg ->
                key(msg.id()) {
                    if (msg is ChatMessage) {
                        Text(
                            text = msg.message()
                        )
                    }
                }
            }
        }

        Row {
            TextField(
                modifier  = Modifier.width(250.dp),
                value = msgText,
                onValueChange = { msgText = it },
                maxLines = 5, singleLine = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )

            Button(modifier = Modifier.wrapContentWidth(),
                enabled = msgText.isNotBlank(),
                onClick = {
                    coroutineScope.launch(Dispatchers.Default) {
                        val chatMsg = ChatMessage.Builder()
                            .setToIdentifier(toSelfId)
                            .setMessage(msgText)
                            .build()

                        msgText = ""
                        messages.add(chatMsg)

                        account.send(message = chatMsg) { }
                    }
                }
            ) {
                Text(text = "Send")
            }
        }
    }
}
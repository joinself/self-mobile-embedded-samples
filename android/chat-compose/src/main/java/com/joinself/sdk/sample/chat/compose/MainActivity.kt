package com.joinself.sdk.sample.chat.compose

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.joinself.sdk.Environment
import com.joinself.sdk.liveness.LivenessCheck
import com.joinself.sdk.models.Account
import com.joinself.sdk.models.Attestation
import com.joinself.sdk.sample.chat.compose.ui.theme.SelfSDKSamplesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

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

        // callback for registration
        var attestationCallBack: ((ByteArray, attesation: Attestation?) -> Unit)? = null

        setContent {
            val coroutineScope = rememberCoroutineScope()
            val navController = rememberNavController()
            var selfId: String? by remember { mutableStateOf(account.identifier()) }
            var showDialog by remember { mutableStateOf(false) }
            var showLocationPermission by remember { mutableStateOf(false) }
            var showLocationDialog = remember { mutableStateOf(false) }
            var locationValue = ""

            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    SelfSDKSamplesTheme {
                        Surface(modifier = Modifier
                            .fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            fun getLocation() {
                                lifecycleScope.launch {
                                    val location = account.location()
                                    locationValue = location.firstOrNull()?.fact()?.value() ?: ""
                                    if (locationValue.isNotEmpty()) {
                                        showLocationDialog.value = true
                                    }
                                    Timber.d("location: ${locationValue}")
                                }
                            }
                            MainView(selfId = selfId,
                                onCreateAccount = {
                                    attestationCallBack = { selfieImage, attestation ->
                                        coroutineScope.launch(Dispatchers.Default) {
                                            if (account.identifier().isNullOrEmpty() && attestation != null) {
                                                showDialog = true
                                                selfId = account.register(selfieImage = selfieImage, selfieAttestation = attestation)
                                                Timber.d("SelfId: $selfId")
                                                showDialog = false
                                            }
                                        }
                                    }
                                    navController.navigate("livenessCheck")
                                },
                                onNavigateToLivenessCheck = {
                                    navController.navigate("livenessCheck")
                                },
                                onNavigateToMessaging = {
                                    navController.navigate("messaging")
                                },
                                onExportBackup = {
                                    lifecycleScope.launch(Dispatchers.Default) {
                                        val backupFile = account.backup()
                                        if (backupFile != null) {
                                            withContext(Dispatchers.Main) {
                                                shareFile(backupFile)
                                            }
                                        }
                                    }
                                },
                                onGetLocation = {
                                    val checkResult = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                    if (checkResult != PackageManager.PERMISSION_GRANTED ) {
                                        showLocationPermission = true
                                        return@MainView
                                    }
                                    getLocation()
                                }
                            )
                            if (showLocationPermission) {
                                LocationView(onPermissionGranted = {
                                    getLocation()
                                })
                            }
                            when {
                                showLocationDialog.value -> {
                                    AlertDialog(
                                        title = { Text(text = "Location") },
                                        text = { Text(text = locationValue) },
                                        onDismissRequest = {},
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    showLocationDialog.value = false
                                                }
                                            ) {
                                                Text("OK")
                                            }
                                        }
                                    )
                                }
                            }

                            ProgressDialog(showDialog)
                        }
                    }
                }
                composable("livenessCheck") {
                    SelfSDKSamplesTheme {
                        Surface(modifier = Modifier
                            .fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            LivenessCheckScreen(account = account, activity = this@MainActivity,
                                onResult = { selfieImage,  attestation ->
                                    if (attestationCallBack != null) {
                                        navController.popBackStack()
                                        attestationCallBack?.invoke(selfieImage, attestation)
                                        attestationCallBack = null
                                    }
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
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

    private fun shareFile(backupFile: File) {
        val uri = FileProvider.getUriForFile(baseContext, baseContext.packageName + ".file_provider", backupFile)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val chooserIntent = Intent.createChooser(intent, "Share file with")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(chooserIntent)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(selfId: String?,
             onCreateAccount: () -> Unit,
             onNavigateToLivenessCheck: () -> Unit,
             onNavigateToMessaging: () -> Unit,
             onExportBackup: () -> Unit,
             onGetLocation: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
    ) {
        TopAppBar(title = { Text(text = "Chat Compose Sample" ) },
            modifier = Modifier.padding(bottom = 16.dp))
        Text(
            text = "SelfId: $selfId",
            textAlign = TextAlign.Center,
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

        Button(onClick = {
            onExportBackup.invoke()
        }, enabled = !selfId.isNullOrEmpty()) {
            Text(text = "Export backup")
        }

        Button(onClick = {
            onGetLocation.invoke()
        }, enabled = !selfId.isNullOrEmpty()) {
            Text(text = "Location")
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationView(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val cameraPermissionState =
        rememberMultiplePermissionsState(permissions = listOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))

    PermissionsRequired(
        multiplePermissionsState = cameraPermissionState,
        permissionsNotGrantedContent = {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchMultiplePermissionRequest()
            }
        },
        permissionsNotAvailableContent = {
            Column {
                Toast.makeText(context, "Permission denied.", Toast.LENGTH_LONG).show()
            }
        },
        content = {
            LaunchedEffect(Unit) {
                Timber.d("location permission granted")
                onPermissionGranted.invoke()
            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LivenessCheckScreen(account: Account, activity: Activity, onResult: (ByteArray, attestation: Attestation?) -> Unit, onBack:()->Unit) {
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
            LivenessCheckView(account = account, activity = activity, onResult, onBack)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivenessCheckView(account: Account, activity: Activity, onResult: (ByteArray, attestation: Attestation?) -> Unit, onBack:()->Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val livenessCheck = LivenessCheck()

    var cameraPreview: com.joinself.sdk.liveness.CameraSourcePreview? = null
    var graphicOverlay: com.joinself.sdk.liveness.GraphicOverlay? = null

    var challenge: LivenessCheck.Challenge? by remember { mutableStateOf(LivenessCheck.Challenge.None) }
    var status: LivenessCheck.Status by remember { mutableStateOf(LivenessCheck.Status.Info) }
    var error: LivenessCheck.Error? by remember { mutableStateOf(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(0.dp)
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

        TopAppBar(
            title = { Text(text = "" ) },
            navigationIcon = {
                IconButton(onClick = { onBack.invoke() }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
            }
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = txt,
            textAlign = TextAlign.Center,
            maxLines = 3, minLines = 2,
            fontWeight = FontWeight.Bold
        )
        Text(modifier = Modifier.padding(top = 4.dp),
            text = "Status: ${error?.name ?: status.name}"
        )
        Surface(
            modifier = Modifier
                .width(300.dp)
                .height(300.dp)
        ) {
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
                onResult = { selfieImage, attestation ->
                    Timber.d("attestation: $attestation")
                    onResult.invoke(selfieImage, attestation)
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

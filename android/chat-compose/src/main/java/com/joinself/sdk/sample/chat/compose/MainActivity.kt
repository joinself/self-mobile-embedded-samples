package com.joinself.sdk.sample.chat.compose

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.joinself.sdk.Environment
import com.joinself.sdk.models.Account
import com.joinself.sdk.models.Attestation
import com.joinself.sdk.sample.chat.compose.ui.theme.SelfSDKSamplesTheme
import com.joinself.sdk.sample.common.FileUtils
import com.joinself.sdk.ui.addLivenessCheckRoute
import com.joinself.sdk.ui.addPassportVerificationRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.UUID

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
        var attestationCallBack: ((ByteArray, attesation: List<Attestation>) -> Unit)? = null

        setContent {
            val coroutineScope = rememberCoroutineScope()
            val navController = rememberNavController()
            var selfId: String? by remember { mutableStateOf(account.identifier()) }
            var showDialog by remember { mutableStateOf(false) }
            var showLocationPermission by remember { mutableStateOf(false) }
            val showLocationDialog = remember { mutableStateOf(false) }
            var showPassportDialog: String? by remember { mutableStateOf(null) }
            var locationValue = ""

            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { fileUri ->
                Timber.d("selected file URI ${fileUri.toString()}")
                if (fileUri != null) {
                    val name = UUID.randomUUID().toString()
                    val rootDir = baseContext.cacheDir
                    val zippedFile = File(rootDir, name)
                    if (zippedFile.exists()) zippedFile.delete()
                    val inputStream = baseContext.contentResolver.openInputStream(fileUri)
                    if (inputStream != null) {
                        FileUtils.writeToFile(inputStream, zippedFile, doProgress = {})
                    }
                    Timber.d("Copy file to ${zippedFile.absolutePath}")
                    if (zippedFile.exists() && zippedFile.length() > 0) {
                        attestationCallBack = { selfieImage, attestations ->
                            coroutineScope.launch(Dispatchers.Default) {
                                try {
                                    account.restore(zippedFile, selfieImage)
                                    Timber.d("Restore successfully")
                                    selfId = account.identifier()
                                } catch (ex: Exception) {
                                    Timber.e(ex)
                                }
                            }
                        }
                        navController.navigate("livenessRoute")
                    }
                }
            }

            NavHost(navController = navController,
                startDestination = "main",
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                composable("main") {
                    SelfSDKSamplesTheme {
                        Surface(
                            modifier = Modifier
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
                                    attestationCallBack = { selfieImage, attestations ->
                                        coroutineScope.launch(Dispatchers.Default) {
                                            if (account.identifier().isNullOrEmpty() && attestations.isNotEmpty()) {
                                                showDialog = true
                                                selfId = account.register(selfieImage = selfieImage, attestations = attestations)
                                                Timber.d("SelfId: $selfId")
                                                showDialog = false
                                            }
                                        }
                                    }
                                    navController.navigate("livenessRoute")
                                },
                                onNavigateToLivenessCheck = {
                                    navController.navigate("livenessRoute")
                                },
                                onNavigateToMessaging = {
                                    navController.navigate("messaging")
                                },
                                onNavigateToPassport = {
                                    navController.navigate("passportRoute")
                                },
                                onNavigateToMobileUI = {
                                    navController.navigate("mobile_ui")
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
                                onImportBackup = {
                                    launcher.launch(arrayOf("application/*"))
                                },
                                onGetLocation = {
                                    val checkResult = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                    if (checkResult != PackageManager.PERMISSION_GRANTED) {
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
                                showPassportDialog != null -> {
                                    AlertDialog(
                                        title = { Text(text = "Passport Verification") },
                                        text = { Text(text = showPassportDialog ?: "") },
                                        onDismissRequest = {},
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    showPassportDialog = null
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

                composable("messaging") {
                    SelfSDKSamplesTheme {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MessagingView(account = account, onBack = {
                                navController.popBackStack()
                            })
                        }
                    }
                }

                composable("mobile_ui") {
                    SelfSDKSamplesTheme {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
//                            TestScreen(this@MainActivity, onBack = {
//                                navController.popBackStack()
//                            })
                        }
                    }
                }

                addLivenessCheckRoute(navController, route = "livenessRoute", account, this@MainActivity, withAttestation = true) { image, attestation ->
                    attestationCallBack?.invoke(image, attestation)
                    attestationCallBack = null
                }
                addPassportVerificationRoute(navController, route = "passportRoute", account, this@MainActivity) { exception ->
                    if (exception == null) {
                        showPassportDialog = "Success"
                    } else {
                        showPassportDialog = "Failed"
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
fun MainView(
    selfId: String?,
    onCreateAccount: () -> Unit,
    onNavigateToLivenessCheck: () -> Unit,
    onNavigateToMessaging: () -> Unit,
    onNavigateToPassport: () -> Unit,
    onNavigateToMobileUI: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onGetLocation: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
    ) {
        TopAppBar(
            title = { Text(text = "Chat Compose Sample") },
            modifier = Modifier.padding(bottom = 16.dp)
        )
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
            onImportBackup.invoke()
        }, enabled = selfId.isNullOrEmpty()) {
            Text(text = "Import backup")
        }

        Button(onClick = {
            onGetLocation.invoke()
        }, enabled = !selfId.isNullOrEmpty()) {
            Text(text = "Location")
        }
        Button(onClick = {
            onNavigateToPassport.invoke()
        }, enabled = !selfId.isNullOrEmpty()) {
            Text(text = "Passport Verification")
        }
//        Button(onClick = {
//            onNavigateToMobileUI.invoke()
//        }, enabled = !selfId.isNullOrEmpty()) {
//            Text(text = "Mobile UI")
//        }
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

@Composable
fun ProgressDialog(showDialog: Boolean) {
    if (showDialog) {
        Dialog(
            onDismissRequest = { },
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                contentAlignment = Alignment.Center,

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

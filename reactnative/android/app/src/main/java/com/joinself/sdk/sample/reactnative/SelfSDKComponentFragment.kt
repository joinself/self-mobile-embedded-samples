package com.joinself.sdk.sample.reactnative
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.joinself.sdk.models.Attestation
import com.joinself.sdk.ui.addLivenessCheckRoute
import com.joinself.sdk.ui.addPassportVerificationRoute

class SelfSDKComponentFragment: Fragment() {
    private lateinit var app: MainApplication

    companion object {
        var onVerificationCallback: ((ByteArray, List<Attestation>) -> Unit)? = null
        private var navBack: (()->Unit)? =  null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = (activity?.application as MainApplication)

        navBack = {
            findNavController().navigateUp()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val route = arguments?.getString("route") ?: "main"

        return ComposeView(requireContext()).apply {
            setContent {
                val coroutineScope = rememberCoroutineScope()
                val navController = rememberNavController()
                var showPassportDialog: String? by remember { mutableStateOf(null) }

                NavHost(navController = navController,
                    startDestination = route,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) {
                    composable("main") {
                        when {
                            showPassportDialog != null -> {
                                AlertDialog(
                                    title = { Text(text = "Passport Verification") },
                                    text = { Text(text = showPassportDialog ?: "") },
                                    onDismissRequest = {},
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                showPassportDialog = null
                                                navBack?.invoke()
                                            }
                                        ) {
                                            Text("OK")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    addLivenessCheckRoute(navController, route = "livenessRoute", app.account, activity = requireActivity(), withAttestation = true) { image, attestation ->
                        onVerificationCallback?.invoke(image, attestation)
                        navBack?.invoke()
                    }

                    addPassportVerificationRoute(navController, route = "passportRoute", app.account, requireActivity()) { exception ->
                        if (exception == null) {
                            showPassportDialog = "Success"
                        } else {
                            showPassportDialog = "Failed"
                        }
                    }
                }
            }
        }
    }
}
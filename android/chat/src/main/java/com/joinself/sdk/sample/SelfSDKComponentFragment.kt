package com.joinself.sdk.sample
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joinself.sdk.models.Attestation
import com.joinself.sdk.sample.chat.R
import com.joinself.sdk.ui.addLivenessCheckRoute
import com.joinself.sdk.ui.addPassportVerificationRoute

class SelfSDKComponentFragment: Fragment() {
    private lateinit var app: App

    companion object {
        var onVerificationCallback: ((ByteArray, List<Attestation>) -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = (activity?.application as App)

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
                NavHost(navController = navController,
                    startDestination = route,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) {
                    composable("main") {
                        Text(text = "Hello SDK")
                    }

                    addLivenessCheckRoute(navController, route = "livenessRoute", app.account, activity = requireActivity(), withAttestation = true) { image, attestation ->
                        onVerificationCallback?.invoke(image, attestation)
                        navController.popBackStack()
                        navController.popBackStack(R.id.selfSDKComponentFragment, inclusive = true)
                    }

                    addPassportVerificationRoute(navController, route = "passportRoute", app.account, requireActivity()) { exception ->
                        if (exception == null) {
//                            showPassportDialog = "Success"
                        } else {
//                            showPassportDialog = "Failed"
                        }
                    }
                }
            }
        }
    }
}
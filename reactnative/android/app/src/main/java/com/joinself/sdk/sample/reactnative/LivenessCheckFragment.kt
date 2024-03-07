package com.joinself.sdk.sample.reactnative

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.joinself.sdk.liveness.LivenessCheck
import com.joinself.sdk.liveness.LivenessCheck.Challenge
import com.joinself.sdk.liveness.LivenessCheck.Status
import com.joinself.sdk.liveness.LivenessCheck.Error
import com.joinself.sdk.models.Account
import com.joinself.sdk.models.Attestation
import com.joinself.sdk.sample.reactnative.databinding.FragmentLivenessCheckBinding
import timber.log.Timber

class LivenessCheckFragment: Fragment() {
    private var _binding: FragmentLivenessCheckBinding? = null
    private val binding get() = _binding!!

    private var progressDialog: Dialog? = null

    private var livenessCheck = LivenessCheck()

    companion object {
         var onVerificationCallback: ((ByteArray, Attestation?) -> Unit)? = null
         lateinit var account: Account
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLivenessCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBottom.visibility = View.INVISIBLE
        binding.buttonBottom.setOnClickListener {
        }
    }

    override fun onResume() {
        super.onResume()

        requestCameraPermission { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Timber.i("onPause")

        progressDialog?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

        livenessCheck.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy")
    }

    private fun onPermissionGranted() {
        livenessCheck.initialize(account, requireActivity(), binding.graphicOverlay, binding.cameraPreview,
            onStatusUpdated = { status ->
                val prefix = "status"
                var msg = ""
                when (status) {
                    Status.Info -> msg = "$prefix:"
                    Status.Passed -> msg = "$prefix: Passed"
                    Status.Error -> msg = "$prefix: Error"
                }
                updateStatus(msg)
            },
            onChallengeChanged = { challenge ->
                binding.descTextView.setTextColor(requireContext().getColor(R.color.colorTextWhite))
                updateDescription(challenge = challenge, error = null)
            },
            onError = {error ->
                updateDescription(challenge = null, error = error)
            },
            onResult = { selfieImage, attestation ->
                if (onVerificationCallback != null) {
                    findNavController().navigateUp()
                    onVerificationCallback?.invoke(selfieImage, attestation)
                    onVerificationCallback = null
                }
            }
        )
        livenessCheck.start()
    }

    fun requestCameraPermission(completion: ((Boolean) -> Unit)?) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            completion?.invoke(true)
            return
        }
        val permissions = arrayOf(Manifest.permission.CAMERA)
        requestPermissions(permissions, Integer.MAX_VALUE)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Integer.MAX_VALUE) {
            onPermissionGranted()
        }
    }

    @UiThread
    private fun updateStatus(msg: String) {
        activity?.runOnUiThread {
            binding.statusTextView.text = msg
        }
    }

    @UiThread
    private fun updateDescription(challenge: Challenge?, error: Error?) {
        activity?.runOnUiThread {
            when (challenge) {
                Challenge.Smile -> {
                    binding.descTextView.text = getString(R.string.msg_liveness_smile)
                }
                Challenge.Blink -> {
                    binding.descTextView.text = getString(R.string.msg_liveness_blink)
                }
                Challenge.TurnLeft -> {
                    binding.descTextView.text = getString(R.string.msg_liveness_turn_left)
                }
                Challenge.TurnRight -> {
                    binding.descTextView.text = getString(R.string.msg_liveness_turn_right)
                }
                Challenge.Done -> {
                    binding.descTextView.text = getString(R.string.thank_you_2)
                }
                else -> {
                    binding.descTextView.text = ""
                }
            }

            when (error) {
                Error.FaceChanged -> {
                    binding.descTextView.text = getString(R.string.error_liveness_out_of_preview)
                }
                Error.OutOfPreview -> {
                    binding.descTextView.text = getString(R.string.msg_liveness_desc)
                }

                else -> {}
            }
        }
    }
}
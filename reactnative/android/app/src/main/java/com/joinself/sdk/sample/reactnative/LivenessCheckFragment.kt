package com.joinself.sdk.sample.reactnative

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.joinself.sdk.liveness.LivenessCheck
import com.joinself.sdk.liveness.LivenessCheck.Challenge
import com.joinself.sdk.liveness.LivenessCheck.Status
import com.joinself.sdk.liveness.LivenessCheck.Error
import com.joinself.sdk.models.Account
import com.joinself.sdk.models.Attestation
import com.joinself.sdk.sample.reactnative.databinding.FragmentLivenessCheckBinding
import timber.log.Timber

class LivenessCheckFragment: BottomSheetDialogFragment() {
    private var _binding: FragmentLivenessCheckBinding? = null
    private val binding get() = _binding!!

    private var progressDialog: Dialog? = null

    private var livenessCheck = LivenessCheck()

    companion object {
         var onVerificationCallback: ((ByteArray, List<Attestation>) -> Unit)? = null
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheetDialog.setOnShowListener {
            val dialog = dialog
            val bottomSheet = dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet) as View

            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            bottomSheet.layoutParams = layoutParams
            bottomSheet.backgroundTintList = context?.getColorStateList(android.R.color.transparent)

            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetDialog.behavior.peekHeight = layoutParams.height
            bottomSheetDialog.behavior.isHideable = false
            bottomSheetDialog.behavior.isDraggable = false

        }
        bottomSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        bottomSheetDialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        return bottomSheetDialog
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

    @UiThread
    private fun onPermissionGranted() {
        activity?.runOnUiThread {
            livenessCheck.initialize(account, requireActivity(), binding.graphicOverlay, binding.cameraPreview,
                onStatusUpdated = { status ->
                    val prefix = "status"
                    var msg = ""
                    when (status) {
                        Status.Info -> msg = "$prefix:"
                        Status.Passed -> msg = "$prefix: Passed"
                        Status.Error -> msg = "$prefix: Error"
                        else -> msg = "$prefix: ${status.name}"
                    }
                    updateStatus(msg)
                },
                onChallengeChanged = { challenge ->
                    binding.descTextView.setTextColor(requireContext().getColor(R.color.colorTextWhite))
                    updateDescription(challenge = challenge, error = null)
                },
                onError = { error ->
                    updateDescription(challenge = null, error = error)
                },
                onResult = { selfieImage, attestations ->
                    findNavController().navigateUp()
                    if (onVerificationCallback != null) {
                        onVerificationCallback?.invoke(selfieImage, attestations)
                        onVerificationCallback = null
                    }
                }
            )
            livenessCheck.start()
        }
    }

    private fun requestCameraPermission(completion: ((Boolean) -> Unit)?) {
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
                Challenge.LookUp-> {
                    binding.descTextView.text = getString(R.string.msg_liveness_look_up)
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
package com.joinself.sdk.sample

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.joinself.sdk.Environment
import com.joinself.sdk.models.Account
import com.joinself.sdk.sample.chat.R
import com.joinself.sdk.sample.chat.databinding.FragmentFirstBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    private val binding get() = _binding!!

    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        account = Account.Builder()
            .setContext(requireContext())
            .setEnvironment(Environment.review)
            .setStoragePath("account1")
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCreate.setOnClickListener {
            LivenessCheckFragment.account = account
            LivenessCheckFragment.onVerificationCallback = { selfieImage, attestation ->
                lifecycleScope.launch(Dispatchers.Default) {
                    if (attestation != null) {
                        val selfId = account.register(selfieImage, attestation)
                        Timber.d("SelfId: $selfId")
                        updateUI()
                    }
                }
            }
            findNavController().navigate(R.id.action_mainFragment_to_livenessCheckFragment)
        }

        binding.buttonSignIn.visibility = View.GONE
        binding.buttonSignIn.setOnClickListener {
            account.signIn()
        }

        binding.buttonCheckLiveness.setOnClickListener {
            LivenessCheckFragment.account = account
            findNavController().navigate(R.id.action_mainFragment_to_livenessCheckFragment)
        }

        binding.buttonSendMessage.setOnClickListener {
            if (!account.identifier().isNullOrEmpty()) {
                ConversationFragment.account = account
                findNavController().navigate(R.id.action_mainFragment_to_conversationFragment)
            }
        }
        binding.buttonVerify.setOnClickListener {

        }

        binding.buttonExportBackup.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Default) {
                val backupFile = account.backup()
                if (backupFile != null) {
                    withContext(Dispatchers.Main) {
                        val uri = FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".file_provider", backupFile)
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "application/*"
                        intent.putExtra(Intent.EXTRA_STREAM, uri)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        requireContext().startActivity(Intent.createChooser(intent, "Share file with"))
                    }
                }
            }
        }

        binding.buttonImportBackup.setOnClickListener {
            LivenessCheckFragment.account = account
            LivenessCheckFragment.onVerificationCallback = { selfieImage, attestation ->
                lifecycleScope.launch(Dispatchers.Default) {
                    if (attestation != null) {
                        try {
                            account.restore(byteArrayOf(), selfieImage)
                        } catch (ex: Exception) {
                            Snackbar.make(binding.root, ex.message.toString(), Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
            findNavController().navigate(R.id.action_mainFragment_to_livenessCheckFragment)
        }

        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @UiThread
    private fun updateUI() {
        activity?.runOnUiThread {
            try {
                val selfId = account.identifier()
                binding.selfIdTextView.text = "SelfId: ${selfId}"
                binding.buttonCreate.isEnabled = selfId.isNullOrEmpty()
                binding.buttonSignIn.isEnabled = selfId.isNullOrEmpty()
                binding.buttonSendMessage.isEnabled = !selfId.isNullOrEmpty()
                binding.buttonVerify.isEnabled = !selfId.isNullOrEmpty()
                binding.buttonExportBackup.isEnabled = !selfId.isNullOrEmpty()
//                binding.buttonImportBackup.isEnabled = selfId.isNullOrEmpty()
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

}
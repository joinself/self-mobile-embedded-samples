package com.joinself.sdk.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.joinself.sdk.Environment
import com.joinself.sdk.models.Account
import com.joinself.sdk.sample.chat.R
import com.joinself.sdk.sample.chat.databinding.FragmentFirstBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            LivenessCheckFragment.onVerificationCallback = { attestation ->
                lifecycleScope.launch(Dispatchers.Default) {
                    if (attestation != null) {
                        val selfId = account.register(attestation)
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
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

}
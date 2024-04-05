package com.joinself.sdk.sample.reactnative

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.facebook.react.ReactFragment
import com.joinself.sdk.Environment
import com.joinself.sdk.models.Account
import com.joinself.sdk.models.KeyValue
import com.joinself.sdk.sample.reactnative.databinding.FragmentMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null

    private val binding get() = _binding!!

    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        account = Account.Builder()
            .setContext(requireContext())
            .setEnvironment(Environment.review)
            .setStoragePath("account1")
            .build()

        account.setDevMode(true)
        insertTestData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startReactNativeFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        removeReactNativeFragment()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        SelfSDKRNModule.createAccountCallback = {
            createAccount()
        }
        SelfSDKRNModule.livenessCheckCallback= {
            openLivenssCheck()
        }
        SelfSDKRNModule.getKeyValueCallback = { key, callback ->
            getKeyValue(key, callback)
        }
    }

    override fun onStop() {
        super.onStop()
    }

    private lateinit var reactNativeFragment: ReactFragment
    private fun startReactNativeFragment() {
        Timber.d("startReactNativeFragment")
        val params = Bundle().apply {
            putString("message", "test")
        }
        reactNativeFragment = ReactFragment.Builder()
            .setComponentName("reactnative")
            .setLaunchOptions(params)
            .setFabricEnabled(true)
            .build()

        binding.reactNativeFragment.visibility = View.VISIBLE
        SelfSDKRNModule.account = account

        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.reactNativeFragment, reactNativeFragment)
            .commit()
    }

    private fun removeReactNativeFragment() {
        Timber.d("removeReactNativeFragment isStateSaved:$isStateSaved")
        try {
            if (this::reactNativeFragment.isInitialized) {
                requireActivity().supportFragmentManager.beginTransaction()
                    .remove(reactNativeFragment)
                    .commit()
                binding.reactNativeFragment.visibility = View.GONE
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }


    @MainThread
    private fun createAccount() {
        activity?.runOnUiThread {
            LivenessCheckFragment.account = account
            LivenessCheckFragment.onVerificationCallback = { selfieImage, attestations ->
                Timber.d("onVerificationCallback")
                lifecycleScope.launch(Dispatchers.Default) {
                    if (attestations.isNotEmpty()) {
                        val selfId = account.register(selfieImage, attestations)
                        Timber.d("SelfId: $selfId")

                        SelfSDKRNModule.instance?.sendSelfId(selfId ?: "")
                    }
                }
            }
            findNavController().navigate(R.id.action_mainFragment_to_livenessCheckFragment)
        }
    }
    @MainThread
    private fun openLivenssCheck() {
        activity?.runOnUiThread {
            LivenessCheckFragment.account = account
            LivenessCheckFragment.onVerificationCallback = { selfieImage, attestations ->
                Timber.d("onVerificationCallback")

            }
            findNavController().navigate(R.id.action_mainFragment_to_livenessCheckFragment)
        }
    }

    @MainThread
    private fun getKeyValue(key: String, callback: ((String?)->Unit)) {
        activity?.runOnUiThread {
            LivenessCheckFragment.account = account
            LivenessCheckFragment.onVerificationCallback = { selfieImage, attestations ->
                Timber.d("onVerificationCallback")
                val value = account.get(key, attestations)
                callback.invoke(value?.value())
            }
            findNavController().navigate(R.id.action_mainFragment_to_livenessCheckFragment)
        }
    }

    private fun insertTestData() {
        val data1 = KeyValue.Builder()
            .setKey("name")
            .setValue("Test User")
            .setSensitive(true)
            .setMime("text/plain")
            .build()
        account.store(data1)
    }
}
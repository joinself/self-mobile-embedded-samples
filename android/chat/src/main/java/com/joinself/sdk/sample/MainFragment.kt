package com.joinself.sdk.sample

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.joinself.sdk.models.KeyValue
import com.joinself.sdk.sample.chat.R
import com.joinself.sdk.sample.chat.databinding.FragmentMainBinding
import com.joinself.sdk.sample.common.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.URLDecoder


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : Fragment() {
    private val REQUEST_CODE_PICK_DOCUMENT = 1002
    private val REQUEST_CODE_LOCATION = 1003
    private var _binding: FragmentMainBinding? = null

    private val binding get() = _binding!!
    private lateinit var app: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = (activity?.application as App)

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

        binding.buttonCreate.setOnClickListener {
            SelfSDKComponentFragment.onVerificationCallback = { selfieImage, attestations ->
                lifecycleScope.launch(Dispatchers.Default) {
                    if (attestations.isNotEmpty()) {
                        val selfId = app.account.register(selfieImage, attestations)
                        Timber.d("SelfId: $selfId")
                        updateUI()
                    }
                }
            }
            try {
                val bundle = bundleOf("route" to "livenessRoute")
                findNavController().navigate(R.id.action_mainFragment_to_selfSDKComponentFragment, bundle)
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }

        binding.buttonSignIn.visibility = View.GONE
        binding.buttonSignIn.setOnClickListener {
            app.account.signIn()
        }

        binding.buttonCheckLiveness.setOnClickListener {
            SelfSDKComponentFragment.onVerificationCallback = { selfieImage, attestations ->
                lifecycleScope.launch(Dispatchers.Default) {
                    if (attestations.isNotEmpty()) {

                    }
                }
            }

            try {
                val bundle = bundleOf("route" to "livenessRoute")
                findNavController().navigate(R.id.action_mainFragment_to_selfSDKComponentFragment, bundle)
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }

        binding.buttonSendMessage.setOnClickListener {
            if (!app.account.identifier().isNullOrEmpty()) {
                findNavController().navigate(R.id.action_mainFragment_to_conversationFragment)
            }
        }
        binding.buttonVerify.setOnClickListener {
            try {
                val bundle = bundleOf("route" to "passportRoute")
                findNavController().navigate(R.id.action_mainFragment_to_selfSDKComponentFragment, bundle)
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }

        binding.buttonExportBackup.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Default) {
                val backupFile = app.account.backup()
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
            openDocumentPicker()
        }

        binding.buttonLocation.setOnClickListener {
            getLocation()
        }

        binding.buttonGetKeyValue.setOnClickListener {
            SelfSDKComponentFragment.onVerificationCallback = { selfieImage, attestations ->
                lifecycleScope.launch(Dispatchers.Default) {
                    if (attestations.isNotEmpty()) {
                        try {
                            val value = app.account.get("name", attestations)
                            Timber.d("key-value: ${value?.value()}")

                            withContext(Dispatchers.Main) {
                                val builder = AlertDialog.Builder(requireContext())
                                builder.setTitle("Key-Value")
                                builder.setMessage("Key: name - Value: ${value?.value()}")
                                builder.setPositiveButton("OK") { dialog, which -> }
                                builder.show()
                            }
                        } catch (ex: Exception) {
                            Timber.e(ex)
                        }
                    }
                }
            }
            try {
                val bundle = bundleOf("route" to "livenessRoute")
                findNavController().navigate(R.id.action_mainFragment_to_selfSDKComponentFragment, bundle)
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }

        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_DOCUMENT) {
            if (data != null && data.clipData != null) { // multiple files
                val mClipData = data.clipData
                val filePaths = mutableListOf<Uri>()
                for (i in 0 until mClipData!!.itemCount) {
                    val item = mClipData.getItemAt(i)
                    val uri = item.uri
                    uri?.let {
                        filePaths.add(it)
                    }
                }
                handleRestoreFromFile(filePaths.first())
            } else if (data?.data != null) {
                val uri = data.data ?: return

                try {
                    handleRestoreFromFile(uri)
                } catch (e: Exception) {

                }
            }
        }
    }

    @UiThread
    private fun updateUI() {
        activity?.runOnUiThread {
            try {
                val selfId = app.account.identifier()
                binding.selfIdTextView.text = "SelfId: ${selfId}"
                binding.buttonCreate.isEnabled = selfId.isNullOrEmpty()
                binding.buttonSignIn.isEnabled = selfId.isNullOrEmpty()
                binding.buttonSendMessage.isEnabled = !selfId.isNullOrEmpty()
                binding.buttonVerify.isEnabled = !selfId.isNullOrEmpty()
                binding.buttonExportBackup.isEnabled = !selfId.isNullOrEmpty()
                binding.buttonImportBackup.isEnabled = selfId.isNullOrEmpty()
                binding.buttonLocation.isEnabled = !selfId.isNullOrEmpty()
                binding.buttonGetKeyValue.isEnabled = !selfId.isNullOrEmpty()
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }


    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, REQUEST_CODE_PICK_DOCUMENT)
    }

    private fun handleRestoreFromFile(uri: Uri) {
        val afterDecode = URLDecoder.decode(uri.path, "UTF-8")
        val name = afterDecode.substring(afterDecode.lastIndexOf('/') + 1)
        val nameParts = name.split(".")

        val rootDir = requireContext().cacheDir
        val zippedFile = File(rootDir, name)
        if (zippedFile.exists()) zippedFile.delete()
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        if (inputStream != null) {
            FileUtils.writeToFile(inputStream, zippedFile, doProgress = {})
        }
        Timber.d("Copy file to ${zippedFile.absolutePath}")
        if (zippedFile.exists() && zippedFile.length() > 0) {
            activity?.runOnUiThread {
                SelfSDKComponentFragment.onVerificationCallback = { selfieImage, attestations ->
                    lifecycleScope.launch(Dispatchers.Default) {
                        if (attestations.isNotEmpty()) {
                            try {
                                app.account.restore(zippedFile, selfieImage)
                                Timber.d("Restore successfully")
                                updateUI()
                            } catch (ex: Exception) {
                                Timber.e(ex)
                                withContext(Dispatchers.Main) {
                                    Snackbar.make(binding.root, ex.message.toString(), Snackbar.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
                try {
                    val bundle = bundleOf("route" to "livenessRoute")
                    findNavController().navigate(R.id.action_mainFragment_to_selfSDKComponentFragment, bundle)
                } catch (ex: Exception) {
                    Timber.e(ex)
                }
            }
        }
    }

    private fun getLocation() {
        if (!checkLocationPermission())  {
            requestLocationPermissions()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val locAttestation = app.account.location()
            Timber.d("loc attestation: ${locAttestation.firstOrNull()?.fact()?.value()}")
            withContext(Dispatchers.Main) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Location")
                builder.setMessage(locAttestation.firstOrNull()?.fact()?.value())
                builder.setPositiveButton("OK") { dialog, which -> }
                builder.show()
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return !(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermissions() {
        requireActivity().requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_CODE_LOCATION)
    }

    private fun insertTestData() {
        val data1 = KeyValue.Builder()
            .setKey("name")
            .setValue("Test User")
            .setSensitive(true)
            .setMime("text/plain")
            .build()
        app.account.store(data1)
    }

    private fun testKeyValueData() {
        val data1 = KeyValue.Builder()
            .setKey("name")
            .setValue("Test User")
            .setSensitive(true)
            .setMime("text/plain")
            .build()
        app.account.store(data1)

        val result = app.account.get("name", listOf())
        assert(data1.value() == result?.value())
        assert(data1.isSensitive() == result?.isSensitive())
    }
}
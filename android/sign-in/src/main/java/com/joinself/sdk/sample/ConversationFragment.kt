package com.joinself.sdk.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.joinself.sdk.models.Account
import com.joinself.sdk.models.Attachment
import com.joinself.sdk.models.AttestationRequest
import com.joinself.sdk.models.AttestationResponse
import com.joinself.sdk.models.ChatMessage
import com.joinself.sdk.models.DataObject
import com.joinself.sdk.models.Fact
import com.joinself.sdk.models.Message
import com.joinself.sdk.models.ResponseStatus
import com.joinself.sdk.models.VerificationRequest
import com.joinself.sdk.models.VerificationResponse
import com.joinself.sdk.sample2.R
import com.joinself.sdk.sample2.databinding.FragmentConversationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date

class ConversationFragment: Fragment() {
    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!


    var messageList: MutableList<Any> = mutableListOf()
    private lateinit var conversationAdapter: ConversationAdapter

    companion object {
        lateinit var account: Account
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        account.setOnMessageListener { message ->
            addMessage(listOf(message))
            if (message is ChatMessage) {
                Timber.d("chatMessage sender:${message.fromIdentifier()} - content: ${message.message()} - attachments: ${message.attachments().size}")
            }
        }
        account.setOnRequestListener { message ->
            addMessage(listOf(message))
            if (message is AttestationRequest) {
                Timber.d("AttestationRequest from:${message.fromIdentifier()} - facts: ${message.facts().map { it.name() }}")
            }
        }
        account.setOnResponseListener { message ->
            addMessage(listOf(message))
            if (message is AttestationResponse) {
                Timber.d("AttestationRequest from:${message.fromIdentifier()} - attestation: ${message.attestations().size}")
            } else if (message is VerificationResponse) {
                Timber.d("VerificationResponse from:${message.fromIdentifier()} - attestation: ${message.attestations().size}")
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonSend.isEnabled = false
        binding.editTextMessage.doOnTextChanged { text, start, before, count ->
            binding.buttonSend.isEnabled = text.toString().trim().isNotEmpty()
        }

        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        conversationAdapter = ConversationAdapter(mySelfId = account.identifier() ?: "")
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = conversationAdapter

        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()
            binding.editTextMessage.text?.clear()

            val receiver = getReceiver()
            val msg = if (messageText.isEmpty()) "android ${Date()}" else messageText

            lifecycleScope.launch (Dispatchers.Default) {
                val attachment = Attachment.Builder()
                    .setData("hello".toByteArray())
                    .setName("test.txt")
                    .build()
                val chatMsg = ChatMessage.Builder()
                    .setToIdentifier(receiver)
                    .setMessage(msg)
//                    .setAttachments(listOf(attachment))
                    .build()

                addMessage(listOf(chatMsg))
                account.send(message = chatMsg) { }
            }
        }

        displayMenu()
    }

    override fun onResume() {
        super.onResume()

        conversationAdapter.submitList(messageList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @UiThread
    private fun addMessage(msg: List<Any>) {
        activity?.runOnUiThread {
            messageList.addAll(msg)
            conversationAdapter.notifyItemInserted(messageList.size - 1)
            scrollToBottom()
        }
    }

    private fun scrollToBottom() {
        try {
            val position = if (conversationAdapter.itemCount > 0) (conversationAdapter.itemCount - 1) else 0
            binding.recyclerView.scrollToPosition(position)
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    private fun getReceiver(): String {
        val selfId = binding.editTextSelfId.text.toString().trim()
        return selfId.ifEmpty {"20084590084"}  //{ "74136454327" }
    }

    private lateinit var factItems: Map<String, String>
    private fun displayMenu() {
        lifecycleScope.launch(Dispatchers.Main) {
            factItems = Utils.fieldOrderList.map {
                Utils.getFactTitleFromKey(requireContext(), it, source = null) to it
            }.toMap()
            val menuHost: MenuHost = requireActivity()
            menuHost.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_chat, menu)
                    val factMenu = menu.findItem(R.id.item_request_fact)
                    if (factMenu != null) {
                        factItems.keys.forEach {
                            factMenu.subMenu?.add(it)
                        }
                    }
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    if (menuItem.itemId == R.id.item_verify_doc) {
                        lifecycleScope.launch(Dispatchers.Default) {
                            val front = DataObject.Builder()
                                .setData("front".toByteArray())
                                .setContentType("image/jpeg")
                                .build()
                            val back = DataObject.Builder()
                                .setData("back".toByteArray())
                                .setContentType("image/jpeg")
                                .build()
                            val proofs = mapOf("document_image_front" to front, "document_image_back" to back)
                            val verificationRequest = VerificationRequest.Builder()
                                .setToIdentifier("self_verification")
                                .setType("driving_license")
                                .setProofs(proofs)
                                .build()
                            account.send(verificationRequest) {

                            }
                        }
                    } else if (menuItem.itemId == R.id.item_all_attestations) {
                        addMessage(account.attestations())
                    } else if (menuItem.itemId == R.id.item_fact_response) {
                        responseAttestationReqest()
                    } else {
                        val factKey = factItems.get(menuItem.title)
                        if (!factKey.isNullOrEmpty()) {
                            lifecycleScope.launch {
                                val fact = Fact.Builder()
                                    .setName(factKey)
                                    .build()
                                val factRequest = AttestationRequest.Builder()
                                    .setToIdentifier(getReceiver())
                                    .setFacts(listOf(fact))
                                    .build()
                                account.send(factRequest) {}
                                addMessage(listOf(factRequest))
                            }
                        }
                    }

                    return true
                }

            }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }
    }

    private fun responseAttestationReqest() {
        val request = messageList.lastOrNull() as? AttestationRequest
        if (request != null) {
            val selfSignedAttestation = account.makeSelfSignedAttestation(source = "user_specified", "surname", "Test User")
            val attestations = account.attestations()
            val att = attestations.firstOrNull { it.fact().name() == request.facts().first().name() }
            if (att != null) {
                val response = request.makeAttestationResponse(ResponseStatus.accepted, attestations = listOf(att))
                lifecycleScope.launch {
                    account.accept(response) {

                    }
                    addMessage(listOf(response))
                }
            }
        }
    }
}
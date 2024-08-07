package com.joinself.sdk.sample.chat.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.joinself.sdk.DocumentDataType
import com.joinself.sdk.DocumentType
import com.joinself.sdk.models.Account
import com.joinself.sdk.models.Attachment
import com.joinself.sdk.models.Attestation
import com.joinself.sdk.models.AttestationRequest
import com.joinself.sdk.models.AttestationResponse
import com.joinself.sdk.models.ChatMessage
import com.joinself.sdk.models.DataObject
import com.joinself.sdk.models.Fact
import com.joinself.sdk.models.Message
import com.joinself.sdk.models.ResponseStatus
import com.joinself.sdk.models.VerificationRequest
import com.joinself.sdk.models.VerificationResponse
import com.joinself.sdk.sample.common.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MessagingView(account: Account, onBack: ()->Unit) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var toSelfId by remember { mutableStateOf("") }
    val mySelfId = account.identifier()
    var msgText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Any>() }

    account.setOnMessageListener {
        messages.add(it)
    }
    account.setOnRequestListener {
        messages.add(it)
    }
    account.setOnResponseListener {
        messages.add(it)
    }

    fun sendFactRequest(factKey: String) {
        coroutineScope.launch {
            val fact = Fact.Builder()
                .setName(factKey)
                .build()
            val factRequest = AttestationRequest.Builder()
                .setToIdentifier(toSelfId)
                .setFacts(listOf(fact))
                .build()
            account.send(factRequest) {}
            messages.add(factRequest)
        }
    }

    fun respondAttestationRequest() {
        val request = messages.lastOrNull() as? AttestationRequest
        if (request != null) {
            val selfSignedAttestation = account.makeSelfSignedAttestation(source = "user_specified", "surname", "Test User")
            val attestations = account.attestations()
            val att = attestations.firstOrNull { it.fact().name() == request.facts().first().name() }
            if (att != null) {
                val response = request.makeAttestationResponse(ResponseStatus.accepted, attestations = listOf(att))
                coroutineScope.launch {
                    account.accept(response) {
                    }
                    messages.add(response)
                }
            }
        }
    }

    fun verifyIdCard() {
        coroutineScope.launch(Dispatchers.Default) {
            val front = DataObject.Builder()
                .setData("front".toByteArray())
                .setContentType("image/jpeg")
                .build()
            val back = DataObject.Builder()
                .setData("back".toByteArray())
                .setContentType("image/jpeg")
                .build()
            val mrz = DataObject.Builder()
                .setData("IDGBR1234567897<<<<<<<<<<<<<<<7704145F1907313GBR<<<<<<<<K<<8HENDERSON<<ELIZABETH<<<<<<<<<<".toByteArray())
                .setContentType("text/plain")
                .build()
            val proofs = mapOf(
                DocumentDataType.DOCUMENT_IMAGE_FRONT to front,
                DocumentDataType.DOCUMENT_IMAGE_BACK to back,
                DocumentDataType.MRZ to mrz)
            val verificationRequest = VerificationRequest.Builder()
                .setType(DocumentType.IDCARD)
                .setProofs(proofs)
                .build()
            account.send(verificationRequest) {

            }
            messages.add(verificationRequest)
        }
    }
    Scaffold(modifier = Modifier
        .padding(0.dp)
        .fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Messaging" ) },
                navigationIcon = {
                    IconButton(onClick = { onBack.invoke() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    OverflowMenu(onRequest = ::sendFactRequest,
                        onAllAttestation = {
                            messages.addAll(account.attestations())
                        },
                        onRespondFactRequest = {
                            respondAttestationRequest()
                        }, onVerifyDoc = {
                            verifyIdCard()
                        }
                    )
                }
            )
        },
        content = { innerPadding ->
            ConstraintLayout(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                val (toSelf, msgList, msgInput, sendBtn) = createRefs()
                Row(
                    modifier = Modifier.constrainAs(toSelf){
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "To SelfId: "
                    )
                    TextField(
                        value = toSelfId,
                        onValueChange = { toSelfId = it },
                        maxLines = 1, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState(), enabled = true)
                        .constrainAs(msgList) {
                            top.linkTo(toSelf.bottom)
                            bottom.linkTo(msgInput.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                            height = Dimension.fillToConstraints
                        }
                ) {
                    messages.forEach { item ->
                        if (item is Message) {
                            key(item.id()) {
                                val title = if (item.fromIdentifier() == mySelfId) "You" else item.fromIdentifier()
                                val msg = when (item) {
                                    is ChatMessage -> {
                                        val msgBuilder = StringBuilder()
                                        msgBuilder.append(item.message())
                                        if (item.attachments().isNotEmpty()) {
                                            val attString = item.attachments().map { "${it.name()} size: ${it.content().size} bytes" }.joinToString(", ")
                                            msgBuilder.appendLine()
                                            msgBuilder.append(attString)
                                        }
                                        msgBuilder.toString()
                                    }
                                    is AttestationRequest -> {
                                        val factString = item.facts().map { it.name() }.joinToString(", ")
                                        "Fact Req: $factString"
                                    }
                                    is AttestationResponse -> {
                                        val factString = item.attestations().map { "${it.fact().name()}:${it.fact().value()}" }.joinToString("\n")
                                        "Fact Resp: ${item.status().name} \n$factString"
                                    }
                                    is VerificationRequest -> {
                                        "Verification Req: ${item.type()}"
                                    }
                                    is VerificationResponse -> {
                                        val factString = item.attestations().map { "${it.fact().name()}:${it.fact().value()}" }.joinToString("\n")
                                        "Verification Resp: ${item.status().name} \n$factString"
                                    }
                                    else -> ""
                                }
                                Column() {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = msg
                                    )
                                }
                            }
                        } else if (item is Attestation) {
                            val sb = StringBuilder()
                            sb.appendLine("${item.fact().name()}:${item.fact().value()}")
                            if (item.attachment() != null) {
                                sb.appendLine("attachment: ${item.attachment()!!.name()}")
                            }
                            Text(
                                text = sb.toString()
                            )
                        }
                    }
                }

                TextField(
                    modifier = Modifier.constrainAs(msgInput){
                        bottom.linkTo(parent.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(sendBtn.start)
                        width = Dimension.fillToConstraints
                    },
                    value = msgText,
                    onValueChange = { msgText = it },
                    maxLines = 5, singleLine = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                )
                Button(
                    modifier = Modifier
                        .constrainAs(sendBtn) {
                            bottom.linkTo(parent.bottom, margin = 8.dp)
                            start.linkTo(msgInput.end)
                            end.linkTo(parent.end)
                            width = Dimension.wrapContent
                        }
                        .wrapContentWidth(),
                    enabled = msgText.isNotBlank(),
                    onClick = {
                        coroutineScope.launch(Dispatchers.Default) {
                            if (toSelfId.isBlank()) {
                                snackbarHostState.showSnackbar("Please enter recipient SelfId ", duration = SnackbarDuration.Short)
                                return@launch
                            }

                            val attachment = Attachment.Builder()
                                .setData("hello".toByteArray())
                                .setName("test.txt")
                                .build()

                            val chatMsg = ChatMessage.Builder()
                                .setToIdentifier(toSelfId)
                                .setMessage(msgText)
                                .build()

                            msgText = ""
                            messages.add(chatMsg)

                            account.send(message = chatMsg) { }
                        }
                    }
                ) {
                    Text(text = "Send")
                }
            }
        }
    )
}

@Composable
fun OverflowMenu(onRequest: (String)->Unit, onAllAttestation: ()->Unit, onRespondFactRequest:()->Unit, onVerifyDoc:()->Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var showFacts by remember { mutableStateOf(false) }

    val factItems = Utils.fieldOrderList.map {
        Utils.getFactTitleFromKey(LocalContext.current, it, source = null) to it
    }.toMap()

    IconButton(onClick = { showMenu = !showMenu }) {
        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = Color.Black)
    }
    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false}) {
        DropdownMenuItem(text = {
            Text(text = "Request Attestation")
        }, onClick = {
            showMenu = false
            showFacts = true
        })
        DropdownMenuItem(text = {
            Text(text = "All Attestations")
        }, onClick = {
            showMenu = false
            onAllAttestation.invoke()
        })
        DropdownMenuItem(text = {
            Text(text = "Respond to Attestation Request")
        }, onClick = {
            showMenu = false
            onRespondFactRequest.invoke()
        })
        DropdownMenuItem(text = {
            Text(text = "Verify Document")
        }, onClick = {
            showMenu = false
            onVerifyDoc.invoke()
        })
    }
    DropdownMenu(expanded = showFacts, onDismissRequest = { showFacts = false}) {
        factItems.keys.forEach {
            DropdownMenuItem(text = {
                Text(text = it)
            }, onClick = {
                showFacts = false
                factItems.get(it)?.let {fact ->
                    onRequest.invoke(fact)
                }
            })
        }
    }
}


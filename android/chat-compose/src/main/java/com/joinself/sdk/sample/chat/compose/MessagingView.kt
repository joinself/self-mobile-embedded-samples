package com.joinself.sdk.sample.chat.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.joinself.sdk.models.Account
import com.joinself.sdk.models.Attachment
import com.joinself.sdk.models.Attestation
import com.joinself.sdk.models.AttestationRequest
import com.joinself.sdk.models.AttestationResponse
import com.joinself.sdk.models.ChatMessage
import com.joinself.sdk.models.Message
import com.joinself.sdk.models.ResponseStatus
import com.joinself.sdk.models.VerificationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingView(account: Account) {
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    var toSelfId by remember { mutableStateOf("20084590084") }
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

    fun responseAttestationReqest() {
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
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        TopAppBar(title = { Text(text = "Messaging" ) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
            },
            actions = {
                OverflowMenu()
            }
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
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
        Column {
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
                            is VerificationResponse -> {
                                val factString = item.attestations().map { "${it.fact().name()}:${it.fact().value()}" }.joinToString("\n")
                                "Verification Resp: ${item.status().name} \n$factString"
                            }
                            else -> ""
                        }
                        Column {
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
                    Text(
                        text = "${item.fact().name()}:${item.fact().value()}"
                    )
                }
            }
        }

        Row {
            TextField(
                modifier  = Modifier.width(250.dp),
                value = msgText,
                onValueChange = { msgText = it },
                maxLines = 5, singleLine = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )

            Button(modifier = Modifier.wrapContentWidth(),
                enabled = msgText.isNotBlank(),
                onClick = {
                    coroutineScope.launch(Dispatchers.Default) {
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
}

@Composable
fun OverflowMenu() {
    var showMenu by remember { mutableStateOf(false) }

    IconButton(onClick = { showMenu = !showMenu }) {
        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = Color.Black)
    }
    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false}) {
        DropdownMenuItem(text = {
            Text(text = "Request Fact")
        }, onClick = {

        })
    }
}


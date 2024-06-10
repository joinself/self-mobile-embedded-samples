# Self Android SDK APIs

### Setup

- Initialize the SDK in app startup
```kotlin
class App: Application() {
    override fun onCreate() {
        super.onCreate()

        SelfSDK.initialize(applicationContext)
    }
}
```

- Setup account and listeners
```kotlin
val account = Account.Builder()
    .setEnvironment(Environment.review)
    .setStoragePath("account1")
    .build()

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
```

### Create account
Create new Self account in Self network. If Self account is created, it will return current SelfId.
It requires an `attesation` from liveness check. See example [Liveness Check](https://github.com/joinself/self-mobile-embedded-samples/blob/main/android/chat/src/main/java/com/joinself/sdk/sample/LivenessCheckFragment.kt)

```kotlin
lifecycleScope.launch(Dispatchers.Default) {
    val selfId = account.register(attestation)
    Timber.d("SelfId: $selfId")
}
```

### APIs

- Create and send a chat message
```kotlin
val attachment = Attachment.Builder()
    .setData("test data".toByteArray())
    .setName("test.txt")
    .build()
val chatMsg = ChatMessage.Builder()
    .setToIdentifier("24520674618")
    .setMessage("hello")
    .setAttachments(listOf(attachment))
    .build()
account.send(message = chatMsg) {
}
```

- Create an attestation request
```kotlin
val fact = Fact.Builder()
    .setName("email")
    .build()
val factRequest = AttestationRequest.Builder()
    .setToIdentifier("24520674618")
    .setFacts(listOf(fact))
    .build()
account.send(factRequest) {}
```

- Response to an attestation request
```kotlin
val request = message as? AttestationRequest
val attestations = account.attestations()
val attestation = attestations.firstOrNull { it.fact().name() == request.facts().first().name() }
if (attestation != null) {
    val response = request.makeAttestationResponse(ResponseStatus.accepted, attestations = listOf(attestation))
    account.accept(response) {}
}
```

- Create a verification request
```kotlin
val front = DataObject.Builder()
    .setData("front_image_bytes".toByteArray())
    .setContentType("image/jpeg")
    .build()
val back = DataObject.Builder()
    .setData("back_images_bytes".toByteArray())
    .setContentType("image/jpeg")
    .build()
val proofs = mapOf(DocumentDataType.DOCUMENT_IMAGE_FRONT to front, DocumentDataType.DOCUMENT_IMAGE_BACK to back)
val verificationRequest = VerificationRequest.Builder()
    .setType(DocumentType.DRIVING_LICENSE)
    .setProofs(proofs)
    .build()
account.send(verificationRequest) {
}
```
The result proofs are sent in `setOnResponseListener` listener

- Sign data with SelfId's keypair
```kotlin
val payload = "hello"
val signedData = account.sign(payload)
```

- Verify data
```kotlin
val verified = account.verify(signedData)
Timber.d("verified $verified")
```

- Sign-in

Once you created Self account in your device. You can sign-in with existing Self account in other apps.
```kotlin
account.signIn()
```

- Backup

Backup data in PDS into a `self_backup` zip file.

```kotlin
val backupFile = account.backup()
Timber.d("backup file ${backupFile.absolutePath}")
```

- Restore

restore PDS data from `self_backup` file. Restore requires selfie image.
```kotlin
SelfSDKComponentFragment.onVerificationCallback = { selfieImage, attestation ->
    lifecycleScope.launch(Dispatchers.Default) {
        try {
            account.restore(backupFile, selfieImage)
            Timber.d("Restore successfully")
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }
}
val bundle = bundleOf("route" to "livenessRoute")
findNavController().navigate(R.id.action_mainFragment_to_selfSDKComponentFragment, bundle)
```

- Location

Get current location

```kotlin
val locAttestation = account.location()
Timber.d("loc attestation: ${locAttestation}")
```

- Key value data
```kotlin
val data1 = KeyValue.Builder()
    .setKey("name")
    .setValue("Test User")
    .setSensitive(true)
    .build()
account.store(data1)

val result = account.get("name")
val deleted = account.remove("name")
```


### UI Component

__Liveness Check Jetpack Compose__  

- You just need to add `liveness check route` to the main navigation host, with a route name.  
After checking successfully, sdk will return a selfie image and an attestation from `self-verification`.

```kotlin
addLivenessCheckRoute(navController, route = "livenessRoute", account, this@MainActivity) { image, attestation ->
}
```

- Then navigate to it `navController.navigate("livenessRoute")`

This is an example how to integrate liveness check flow into main navigation.
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup account
        val account = Account.Builder()
            .setContext(this)
            .setEnvironment(Environment.review)
            .setStoragePath("account1")
            .build()
        account.setDevMode(true)

        // callback for registration
        var attestationCallBack: ((ByteArray, attesation: List<Attestation>) -> Unit)? = null

        setContent {
            val coroutineScope = rememberCoroutineScope()
            val navController = rememberNavController()

            NavHost(navController = navController,
                startDestination = "main",
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                composable("main") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Button(onClick = {
                            navController.navigate("livenessRoute")
                        }, enabled = true) {
                            Text(text = "Liveness Check")
                        }
                        Button(onClick = {
                            attestationCallBack = { selfieImage, attestations ->
                                coroutineScope.launch(Dispatchers.Default) {
                                    if (account.identifier().isNullOrEmpty() && attestations.isNotEmpty()) {
                                        val selfId = account.register(selfieImage = selfieImage, attestations = attestations)
                                    }
                                }
                            }
                            navController.navigate("livenessRoute")
                        }, enabled = true) {
                            Text(text = "Create Account")
                        }
                    }
                }

                // integrate liveness check from Self SDK
                addLivenessCheckRoute(navController, "livenessRoute", account, this@MainActivity) { image, attestation ->
                    attestationCallBack?.invoke(image, attestation)
                    attestationCallBack = null
                }
            }
        }
    }
}
```
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
LivenessCheckFragment.account = account
LivenessCheckFragment.onVerificationCallback = { selfieImage, attestation ->
    lifecycleScope.launch(Dispatchers.Default) {
        try {
            account.restore(backupFile, selfieImage)
            Timber.d("Restore successfully")
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }
}
findNavController().navigate(R.id.action_mainFragment_to_livenessCheckFragment)
```

- Location

Get current location

```kotlin
val locAttestation = account.location()
Timber.d("loc attestation: ${locAttestation}")
```
# Self iOS SDK APIs

### Setup

- Initialize the SDK in AppDelegate
```swift
func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    SelfSDK.initialize()
    
    return true
}
```

- Setup account and listeners
```swift
let account = Account.Builder()
        .withEnvironment(Environment.review)
        .withStoragePath("account1")       
        .build()
account.setOnMessageListener { msg in    
    if let chatMsg = msg as? ChatMessage {
        log.debug("chatMessage sender:\(msg.fromIdentifier()) - content:\(chatMsg.message()) - attachments: \(chatMsg.attachments().count)")
    }
}
account.setOnRequestListener { msg in    
    if let request = msg as? AttestationRequest {
        log.debug("AttestationRequest from:\(request.fromIdentifier()) - fact:\(request.facts().map{$0.name()})")
    }
}
account.setOnResponseListener { msg in    
    if let response = msg as? AttestationResponse {
        log.debug("AttestationResponse from:\(response.fromIdentifier()) - status:\(response.status().rawValue) - attestation:\(response.attestations().map{$0.fact().value()})")
    } else if let response = msg as? VerificationResponse {
        log.debug("VerificationResponse from:\(response.fromIdentifier()) - status:\(response.status().rawValue) - attestation:\(response.attestations().map{$0.fact().value()})")
    }
}
```

### Create account
Create new Self account in Self network. If Self account is created, it will return current SelfId.

```swift
Task {
    let selfId = await account.register(attestation: attestation)
    log.debug("SelfId: \(selfId)")           
}
```

### APIs

- Create and send a chat message
```swift
var attachments: [Attachment] = []
if let data = "test data".data(using: .utf8) {
    let attachment = Attachment.Builder()
        .withData(data)
        .withName("test.txt")
        .build()
    attachments.append(attachment)
}
        
let chatMsg = ChatMessage.Builder()
    .toIdentifier("58141443814")
    .withMessage("hello")
    .withAttachments(attachments)
    .build()                
account.send(message: chatMsg, onAcknowledgement: {error in        
})
```

- Create an attestation request
```swift
let fact = Fact.Builder()
    .withName("email")
    .build()
let factRequest = AttestationRequest.Builder()
    .toIdentifier(recipient)
    .withFacts([fact])
    .build()

Task {
    try await account.send(message: factRequest, onAcknowledgement: {error in
    })
}
```

- Response to an attestation request
```swift
if let request = messages as? AttestationRequest {
    let attestations = account.attestations()
    if let attestation = attestations.first {$0.fact().name() == request.facts().first?.name()} {
        let response = request.makeAttestationResponse(status: .accepted, attestations: [attestation])
        Task {
            try await account.accept(message: response, onAcknowledgement: {error in
            })
        }
    }
}
```

- Create an verification request
```swift
var proofs: [String: DataObject] = [:]
if let data = "front".data(using: .utf8) {
    let front = DataObject.Builder()
        .withData(data)
        .withContentType("image/jpeg")
        .build()
    proofs[DocumentDataType.DOCUMENT_IMAGE_FRONT] = front
}
if let data = "back".data(using: .utf8) {
    let back = DataObject.Builder()
        .withData(data)
        .withContentType("image/jpeg")
        .build()
    proofs[DocumentDataType.DOCUMENT_IMAGE_BACK] = back
}

let verificationRequest = VerificationRequest.Builder()   
    .withType(DocumentType.DRIVING_LICENSE)
    .withProofs(proofs)
    .build()
Task {
    try await account.send(message: verificationRequest, onAcknowledgement: {error in
    })
}
```

- Sign data with SelfId's keypair
```swift
let payload = "hello"
let signedData = account.sign(payload: payload)
```

- Verify data
```swift
let verified = account.verify(signable: signedData)
```

- Sign-in




- Backup

Backup data in PDS into a `self_backup` zip file.

```swift
if let fileUrl = await account.backup() {
    print("backup file: \(fileUrl.path)")
}
```

- Restore

restore PDS data from `self_backup` file. Restore requires selfie image.

```swift
let vc = LivenessCheckViewController.instantiate(from: .Main)
vc.account = self.account
vc.onFinishCallback = {selfieImage, attestation in
    Task {
        await self.account.restore(backupFile: selectedFileURL, selfieImage: selfieImage)        
    }
}
self.navigationController?.pushViewController(vc, animated: true)
```

- Location

Get current location

```swift
Task {
    let locAttestation = try! await self.account.location()
    print("Location fact: \(locAttestation)")
}
```
//
//  ChatViewModel.swift
//  self-ios-sample
//
//  Created by Long Pham on 03/10/2023.
//

import Combine
import self_ios_sdk

class ChatViewModel: ObservableObject {
    private let account: Account
    struct MsgData: Identifiable {
         let id = UUID()
         var message: Message? = nil
        var attestation: Attestation? = nil
     }
    
    @Published var messages: [MsgData] = []
    
    init(account: Account) {
        self.account = account
        
        self.account.setOnMessageListener { msg in
            self.addMessage(msg: msg)
            if let chatMsg = msg as? ChatMessage {
                log.debug("chatMessage sender:\(msg.fromIdentifier()) - content:\(chatMsg.message()) - attachments: \(chatMsg.attachments().count)")
            }
        }
        self.account.setOnRequestListener { msg in
            self.addMessage(msg: msg)
            if let request = msg as? AttestationRequest {
                log.debug("AttestationRequest from:\(request.fromIdentifier()) - fact:\(request.facts().map{$0.name()})")
            }
        }
        self.account.setOnResponseListener { msg in
            self.addMessage(msg: msg)
            if let response = msg as? AttestationResponse {
                log.debug("AttestationResponse from:\(response.fromIdentifier()) - status:\(response.status().rawValue) - attestation:\(response.attestations().map{$0.fact().value()})")
            } else if let response = msg as? VerificationResponse {
                log.debug("VerificationResponse from:\(response.fromIdentifier()) - status:\(response.status().rawValue) - attestation:\(response.attestations().map{$0.fact().value()})")
            }
        }
    }
    
    func sendMessage(message: String, recipient: String) {
        do {
            let msg = message//"ios \(TimeUtils.now().toRFC3339String())"
            let receiver = recipient
            
            var attachments: [Attachment] = []
            if let data = "hello".data(using: .utf8) {
                let attachment = Attachment.Builder()
                    .withData(data)
                    .withName("test.txt")
                    .build()
                attachments.append(attachment)
            }
            
            let chatMsg = ChatMessage.Builder()
                .toIdentifier(receiver)
                .withMessage(msg)
//                .withAttachments(attachments)
                .build()
            
            Task {
                try await account.send(message: chatMsg, onAcknowledgement: {error in })
            }
            addMessage(msg: chatMsg)
        } catch {
            
        }
    }
    
    func clearChat() -> () {
        self.messages = []
    }
    
    func addMessage(msg: Message) {
        ez.runThisInMainThread {
            self.messages.append(MsgData(message: msg))
        }
    }
    func addAttestation(att: Attestation) {
        ez.runThisInMainThread {
            self.messages.append(MsgData(attestation: att))
        }
    }
    
    // MARK: - Fact Requests
    func getAllAttestations() {
        let attestations = account.attestations()
        attestations.forEach {
            self.addAttestation(att: $0)
        }
    }
    
    func requestFact(recipient: String, fact: String) -> () {
        let fact = Fact.Builder()
            .withName(fact)
            .build()
        let factRequest = AttestationRequest.Builder()
            .toIdentifier(recipient)
            .withFacts([fact])
            .build()
        
        Task {
            try await account.send(message: factRequest, onAcknowledgement: {error in
            })
        }
        addMessage(msg: factRequest)
    }
    
    func responseFactRequest() -> () {
        if let request = messages.last?.message as? AttestationRequest {
            let attestations = account.attestations()
            let att = attestations.first {$0.fact().name() == request.facts().first?.name()}
            let selfSignedAttestation = account.makeSelfSignedAttestation(source: "user_specified", name: "surname", value: "Test User")
            if att != nil {
                let response = request.makeAttestationResponse(status: .accepted, attestations: [att!])
                addMessage(msg: response)
                Task {
                    try await account.accept(message: response, onAcknowledgement: {error in
                    })
                }
            }
        }
    }
    
    func responseFactRequest(request: AttestationRequest, att: Attestation) -> () {
        let response = request.makeAttestationResponse(status: .accepted, attestations: [att])
        addMessage(msg: response)
        Task {
            try await account.accept(message: response, onAcknowledgement: {error in
            })
        }
    }
    
    func verifyIDCard() -> () {
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
        if let data = "IDGBR1234567897<<<<<<<<<<<<<<<7704145F1907313GBR<<<<<<<<K<<8HENDERSON<<ELIZABETH<<<<<<<<<<".data(using: .utf8) {
            let mrz = DataObject.Builder()
                .withData(data)
                .withContentType("text/plain")
                .build()
            proofs[DocumentDataType.MRZ] = mrz
        }
        let verificationRequest = VerificationRequest.Builder()
            .withType(DocumentType.IDCARD)
            .withProofs(proofs)
            .build()
        Task {
            try await account.send(message: verificationRequest, onAcknowledgement: {error in
            })
        }
        
        addMessage(msg: verificationRequest)
    }
    
    func verifyDrivingLicense() -> () {
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
        
        addMessage(msg: verificationRequest)
    }
    func verifyPasspord() -> () {
        var proofs: [String: DataObject] = [:]
        if let data = "dg1".data(using: .utf8) {
            let dg1 = DataObject.Builder()
                .withData(data)
                .withContentType("application/x-binary")
                .build()
            proofs[DocumentDataType.DG1] = dg1
        }
        if let data = "dg2".data(using: .utf8) {
            let dg2 = DataObject.Builder()
                .withData(data)
                .withContentType("application/x-binary")
                .build()
            proofs[DocumentDataType.DG2] = dg2
        }
        if let data = "sod".data(using: .utf8) {
            let sod = DataObject.Builder()
                .withData(data)
                .withContentType("application/x-binary")
                .build()
            proofs[DocumentDataType.SOD] = sod
        }
        let verificationRequest = VerificationRequest.Builder()
            .withType(DocumentType.PASSPORT)
            .withProofs(proofs)
            .build()
        Task {
            try await account.send(message: verificationRequest, onAcknowledgement: {error in
            })
        }
        
        addMessage(msg: verificationRequest)
    }
    
    func signData() {
        let payload = "hello"
        if let signable = account.sign(payload: payload) {
            let verified = account.verify(signable: signable)
            
            log.debug("verified \(verified)")
        }
        
        if let response = messages.last as? AttestationResponse {
            if let attestation = response.attestations().first {
                let verified = account.verify(signable: attestation)
                log.debug("verified \(verified)")
            }
        }
//        Task {
//            if let data = "hello".data(using: .utf8) {
//                let dataObject = DataObject.Builder()
//                    .withData(data)
//                    .withContentType("text/plain")
//                    .build()
//                if let dataLink = try await account.upload(dataObject: dataObject) {
//                    let result = try await account.download(dataLink: dataLink)
//                }
//            }
//        }
    }
}

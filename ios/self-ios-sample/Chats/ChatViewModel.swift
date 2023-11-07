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
    
    @Published var messages: [Message] = []
    
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
            self.messages.append(msg)
        }
    }
    // MARK: - Fact Requests
    func requestFact(recipient: String) -> () {
        let fact = Fact.Builder()
            .withName("phone_number")
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
    
    func responseFactRequest(recipient: String) -> () {
        if let request = messages.last as? AttestationRequest {
            let attestations = account.attestations()
            let att = attestations.first {$0.fact().name() == request.facts().first?.name()}
            if let selfSignedAttestation = account.makeSelfSignedAttestation(source: "user_specified", name: "surname", value: "Test User") {
                let response = request.makeAttestationResponse(status: .accepted, attestations: [selfSignedAttestation])
                Task {
                    try await account.accept(message: response, onAcknowledgement: {error in
                    })
                }                
            }
        }
    }
    
    func requestVerification() -> () {
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
    }
}

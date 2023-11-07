//
//  ViewController.swift
//  self-ios-sample
//
//  Created by DO HAI VU on 06/09/2023.
//

import UIKit
import SwiftUI
import self_ios_sdk


class ViewController: UIViewController {

    @IBOutlet weak var lblInfo: UILabel!
    @IBOutlet weak var btnLiveness: UIButton!
    @IBOutlet weak var btnCreate: UIButton!
    @IBOutlet weak var btnSendMessage: UIButton!
    
    private var account: Account!
    private var message: Message? = nil
    
    private var onMessage: ((Message) -> Void)!
    private var onRequest: ((Message) -> Void)!
    private var onResponse: ((Message) -> Void)!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        lblInfo.textColor = .black
        lblInfo.font = UIFont.boldSystemFont(ofSize: 17)
        
        btnLiveness.addTarget(self, action: #selector(onLivenessPressed(_:)), for: .touchUpInside)
        btnCreate.addTarget(self, action: #selector(onCreatePressed(_:)), for: .touchUpInside)
        btnSendMessage.addTarget(self, action: #selector(onSendMessagePressed(_:)), for: .touchUpInside)
        
        onMessage = { msg in
            if let chatMsg = msg as? ChatMessage {
                log.debug("chatMessage sender:\(msg.fromIdentifier()) - content:\(chatMsg.message()) - attachments: \(chatMsg.attachments().count)")
            }
        }
        onRequest = { msg in
            self.message = msg
            if let request = msg as? AttestationRequest {
                log.debug("AttestationRequest from:\(request.fromIdentifier()) - fact:\(request.facts().map{$0.name()})")
            }
        }
        onResponse = { msg in
            if let response = msg as? AttestationResponse {
                log.debug("AttestationResponse from:\(response.fromIdentifier()) - status:\(response.status().rawValue) - attestation:\(response.attestations().map{$0.fact().value()})")
            }
        }
        
        account = Account.Builder()
            .withEnvironment(Environment.review)
            .withStoragePath("account1")
            .build()
        
        updateUI()
    }
    
    @objc func onLivenessPressed(_ sender: Any) {
        let vc = LivenessCheckViewController.instantiate(from: .Main)
        vc.account = self.account
        self.navigationController?.pushViewController(vc, animated: true)
    }

    @objc func onCreatePressed(_ sender: Any) {
        
        
        let vc = LivenessCheckViewController.instantiate(from: .Main)
        vc.account = self.account
        vc.onFinishCallback = { attestation in
            Task {
                if let attestation = attestation {
                    let selfId = try! await self.account.register(attestation: attestation)
                    log.debug("SelfId: \(selfId)")
                    
                    self.updateUI()
                }
            }
        }
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    @objc func onSendMessagePressed(_ sender: Any) {
        log.debug("Open Chat.")
        self.openChatView()
        return
        do {
            let msg = "ios \(Date().toRFC3339String())"
            let receiver = "20084590084"
            
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
            
            let fact = Fact.Builder()
                .withName("phone_number")
                .build()
            let factRequest = AttestationRequest.Builder()
                .toIdentifier(receiver)
                .withFacts([fact])
                .build()
            
//            Task {
//                try await account.send(message: factRequest, onAcknowledgement: {error in
//                })
//            }
//            if let messsage = message {
//                Task {
//                    try await account.accept(message: messsage, onAcknowledgement: {error in })
//                }
//            }
            
            // verification
            var proofs: [String: DataObject] = [:]
            if let data = "front".data(using: .utf8) {
                let front = DataObject.Builder()
                    .withData(data)
                    .withContentType("image/jpeg")
                    .build()
                proofs["document_image_front"] = front
            }
            if let data = "back".data(using: .utf8) {
                let back = DataObject.Builder()
                    .withData(data)
                    .withContentType("image/jpeg")
                    .build()
                proofs["document_image_back"] = back
            }
            
            let verificationRequest = VerificationRequest.Builder()
                .toIdentifier("self_verification")
                .withType("driving_license")
                .withProofs(proofs)
                .build()
//            Task {
//                try await account.send(message: verificationRequest, onAcknowledgement: {error in
//                })
//            }
        } catch {
            
        }
    }
    
    private func updateUI() {
        ez.runThisInMainThread {
            if let selfId = self.account.identifier() {
                self.lblInfo.text = "SelfId: \(selfId)"
                self.btnCreate.isEnabled = false            
            } else {
                self.btnCreate.isEnabled = true
            }
        }
    }
    
    private func openChatView() {
        let chatView = ChatView(account: account)
        let vc = UIHostingController(rootView: chatView)
        self.navigationController?.pushViewController(vc, animated: true)
    }
}


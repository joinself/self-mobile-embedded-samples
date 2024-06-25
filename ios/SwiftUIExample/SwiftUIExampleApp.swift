//
//  SwiftUIExampleApp.swift
//  SwiftUIExample
//
//  Created by Long Pham on 24/6/24.
//

import SwiftUI
import self_ios_sdk

@main
struct SwiftUIExampleApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView().onAppear(perform: {
                SelfSDK.initialize()
                setup()
            })
        }
    }
    
    func setup() {
        let account = Account.Builder()
                .withEnvironment(Environment.review)
                .withStoragePath("account1")
                .build()
        account.setOnMessageListener { msg in
            if let chatMsg = msg as? ChatMessage {
                print("chatMessage sender:\(msg.fromIdentifier()) - content:\(chatMsg.message()) - attachments: \(chatMsg.attachments().count)")
            }
        }
        account.setOnRequestListener { msg in
            if let request = msg as? AttestationRequest {
                print("AttestationRequest from:\(request.fromIdentifier()) - fact:\(request.facts().map{$0.name()})")
            }
        }
        account.setOnResponseListener { msg in
            if let response = msg as? AttestationResponse {
                print("AttestationResponse from:\(response.fromIdentifier()) - status:\(response.status().rawValue) - attestation:\(response.attestations().map{$0.fact().value()})")
            } else if let response = msg as? VerificationResponse {
                print("VerificationResponse from:\(response.fromIdentifier()) - status:\(response.status().rawValue) - attestation:\(response.attestations().map{$0.fact().value()})")
            }
        }
    }
}

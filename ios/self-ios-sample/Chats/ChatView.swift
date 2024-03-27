//
//  ChatView.swift
//  self-ios-sample
//
//  Created by Long Pham on 03/10/2023.
//

import SwiftUI
import self_ios_sdk

struct ChatView: View {
    let account: Account
    @State private var attestationRequest: AttestationRequest? = nil
    @State private var locationAttesttation: Attestation? = nil
    @State private var recipient = ""
    @State private var message = ""
    @State private var showingAlert = false
    @State private var showingLocationAlert = false
    @ObservedObject private var viewModel: ChatViewModel
    
    init(account: Account) {
        self.account = account
        viewModel = ChatViewModel(account: account)
    }
    
    var body: some View {
        
        VStack {
            Menu("Options") {
                Button("Get all Attestations", action: {
                    viewModel.getAllAttestations()
                })
                Button("Test Verification", action: {
                    viewModel.verifyDrivingLicense()
                })
                Button("Response to attestation request", action: {
                    if let factRequest = viewModel.messages.last?.message as? AttestationRequest, factRequest.facts().first?.name() == Constants.CLAIM_KEY_LOCATION {
                        Task {
                            let locAttestation = try! await self.account.location()
                            locationAttesttation = locAttestation.first
                            attestationRequest = factRequest
                            showingLocationAlert = true
                            print("Location fact: \(locAttestation.first?.fact().value())")
                            
                        }
                    } else {
                        viewModel.responseFactRequest()
                    }
                })
                Menu("Request attestation") {
                    Button("Location", action: {
                        if recipient.isEmpty {
                            showingAlert = true
                        } else {
                            viewModel.requestFact(recipient: recipient, fact: Constants.CLAIM_KEY_LOCATION)
                        }
                    })
                    Button("Phone number", action: {
                        if recipient.isEmpty {
                            showingAlert = true
                        } else {
                            viewModel.requestFact(recipient: recipient, fact: Constants.CLAIM_KEY_UNVERIFIED_PHONE)
                        }
                    })
                    Button("Email", action: {
                        if recipient.isEmpty {
                            showingAlert = true
                        } else {
                            viewModel.requestFact(recipient: recipient, fact: Constants.CLAIM_KEY_EMAIL)
                        }
                    })
                }
            }
            
            HStack {
                TextField("Enter a recipient's selfId", text: $recipient)
                    .padding(10)
                    .background(Color.secondary.opacity(0.2))
                    .cornerRadius(5)
                Button(action: {
                    recipient = ""
                    viewModel.clearChat()
                }) {
                    Image(systemName: "xmark")
                        .font(.system(size: 20))
                }
                .padding()
                .disabled(recipient.isEmpty)
            }.padding()
            
            
            // Chat history.
            List {
                ForEach(viewModel.messages) { msgData in
                    VStack(alignment: .leading) {
                        if let msg = msgData.message {
                            if msg.fromIdentifier().isEmpty {
                                Text("You").bold()
                            } else {
                                Text(msg.fromIdentifier()).bold()
                            }
                            if let chatMsg = msg as? ChatMessage {
                                Text("\(chatMsg.message())")
                                    .font(.subheadline)
                                if !chatMsg.attachments().isEmpty {
                                    let attachmentInfo = chatMsg.attachments().map{ "\($0.name()) \($0.content().count) bytes"}.joined(separator: ";")
                                    Text(attachmentInfo)
                                        .font(.subheadline)
                                }
                            } else if let request = msg as? AttestationRequest {
                                Text("Attestation Req: \(request.facts().map{$0.name()}.joined(separator: ","))")
                                    .font(.subheadline)
                            } else if let response = msg as? AttestationResponse {
                                Text("Attestation Resp: \(response.status().rawValue)\n\(response.attestations().map{"\($0.fact().name()):\($0.fact().value())"}.joined(separator: "\n"))")
                                    .font(.subheadline)
                            } else if let request = msg as? VerificationRequest {
                                Text("Verification Req: \(request.type())")
                                    .font(.subheadline)
                            } else if let response = msg as? VerificationResponse {
                                Text("Verification Resp: \(response.status().rawValue)\n\(response.attestations().map{"\($0.fact().name()):\($0.fact().value())"}.joined(separator: "\n"))")
                                    .font(.subheadline)
                            }
                        } else if let att = msgData.attestation {
                            Text("\(att.fact().name()):\(att.fact().value())")
                                .font(.subheadline)
                        }
                    }
                }
            }

            // Message field.
            HStack {
                TextField("Write a message", text: $message)
                    .padding(10)
                    .background(Color.secondary.opacity(0.2))
                    .cornerRadius(5)
                
                Button(action: {
                    if recipient.isEmpty {
                        showingAlert = true
                    } else {
                        viewModel.sendMessage(message: message, recipient: recipient)
                        message = ""
                    }
                    
                }) {
                    Image(systemName: "paperplane.fill")
                        .font(.system(size: 20))
                }
                .padding()
                .disabled(message.isEmpty)
                .alert(isPresented: $showingAlert) {
                            Alert(title: Text("Warning!"), message: Text("Please provider a recipient's SelfId"), dismissButton: .default(Text("Got it!")))
                        }
                .alert(isPresented: $showingLocationAlert, content: {
                    Alert(title: Text("Share location"),
                          message: Text("\(locationAttesttation?.fact().value() ?? "" )"),
                          primaryButton: .default(
                                          Text("OK"),
                                          action: {
                                              if let locationAttesttation = locationAttesttation, let attestationRequest = attestationRequest {
                                                  viewModel.responseFactRequest(request: attestationRequest, att: locationAttesttation)
                                              }
                                          }
                                      ),
                          secondaryButton: .cancel()
                    )
                                        
                })
            }
            .padding()
        }
    }
}

//#Preview {
//    ChatView()
//}

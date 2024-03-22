//
//  ViewController.swift
//  self-ios-sample
//
//  Created by DO HAI VU on 06/09/2023.
//

import UIKit
import SwiftUI
import self_ios_sdk
import UniformTypeIdentifiers
import CoreLocation

class ViewController: UIViewController {

    @IBOutlet weak var lblInfo: UILabel!
    @IBOutlet weak var btnLiveness: UIButton!
    @IBOutlet weak var btnCreate: UIButton!
    @IBOutlet weak var btnSendMessage: UIButton!
    @IBOutlet weak var btnExportBackup: UIButton!
    @IBOutlet weak var btnImportBackup: UIButton!
    @IBOutlet weak var btnLocation: UIButton!
    
    private var account: Account!
    private var message: Message? = nil
    
    private var onMessage: ((Message) -> Void)!
    private var onRequest: ((Message) -> Void)!
    private var onResponse: ((Message) -> Void)!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        btnLiveness.addTarget(self, action: #selector(onLivenessPressed(_:)), for: .touchUpInside)
        btnCreate.addTarget(self, action: #selector(onCreatePressed(_:)), for: .touchUpInside)
        btnSendMessage.addTarget(self, action: #selector(onSendMessagePressed(_:)), for: .touchUpInside)
        btnExportBackup.addTarget(self, action: #selector(onExportBackupPressed(_:)), for: .touchUpInside)
        btnImportBackup.addTarget(self, action: #selector(onImportBackupPressed(_:)), for: .touchUpInside)
        btnLocation.addTarget(self, action: #selector(onLocationPressed(_:)), for: .touchUpInside)
        
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
        vc.onFinishCallback = {selfieImage, attestation in
            
        }
        self.navigationController?.pushViewController(vc, animated: true)
    }

    @objc func onCreatePressed(_ sender: Any) {
        let vc = LivenessCheckViewController.instantiate(from: .Main)
        vc.account = self.account
        vc.onFinishCallback = {selfieImage, attestation in
            Task {
                if let attestation = attestation {
                    let selfId = try! await self.account.register(selfieImage: selfieImage, attestation: attestation)
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
    }
    
    @objc func onExportBackupPressed(_ sender: Any) {
        print("onExportBackupPressed")
        Task {
            if let fileUrl = await account.backup() {
                print("onExportBackupPressed: \(fileUrl.path)")
                shareFile(fileURL: fileUrl)
            }
        }
    }
    
    @objc func onImportBackupPressed(_ sender: Any) {
        let supportedTypes: [UTType] = [UTType.init(filenameExtension: "self_backup")! as UTType]
        let documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: supportedTypes)
        documentPicker.delegate = self
        documentPicker.allowsMultipleSelection = false
        present(documentPicker, animated: true, completion: nil)
    }
    
    @objc func onLocationPressed(_ sender: Any) {
        let locationManager = CLLocationManager()
        if (locationManager.authorizationStatus == .notDetermined ||
            locationManager.authorizationStatus == .denied ||
            locationManager.authorizationStatus == .restricted) {
            locationManager.requestAlwaysAuthorization()
            locationManager.requestWhenInUseAuthorization()
            return
        }
        
        Task {
            let locAttestation = try! await self.account.location()
            print("Location fact: \(locAttestation)")
            if let fact = locAttestation.first?.fact() {
                DispatchQueue.main.async {
                    let alert = UIAlertController(title: "Location", message: fact.value(), preferredStyle: UIAlertController.Style.alert)
                    alert.addAction(UIAlertAction(title: "OK", style: UIAlertAction.Style.default, handler: nil))
                    self.present(alert, animated: true, completion: nil)
                }
            }
        }
    }
    
    private func restoreFromURL(selectedFileURL: URL) {
        let vc = LivenessCheckViewController.instantiate(from: .Main)
        vc.account = self.account
        vc.onFinishCallback = {selfieImage, attestation in
            Task {
                await self.account.restore(backupFile: selectedFileURL, selfieImage: selfieImage)
                self.updateUI()
            }
        }
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    private func updateUI() {
        ez.runThisInMainThread {
            if let selfId = self.account.identifier() {
                self.lblInfo.text = "SelfId: \(selfId)"
                self.btnCreate.isEnabled = false
                self.btnSendMessage.isEnabled = true
                self.btnExportBackup.isEnabled = true
                self.btnImportBackup.isEnabled = false
                self.btnLocation.isEnabled = true
            } else {
                self.btnCreate.isEnabled = true
                self.btnSendMessage.isEnabled = false
                self.btnExportBackup.isEnabled = false
                self.btnImportBackup.isEnabled = true
                self.btnLocation.isEnabled = false
            }
        }
    }
    
    private func openChatView() {
        let chatView = ChatView(account: account)
        let vc = UIHostingController(rootView: chatView)
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    private func shareFile(fileURL: URL) {
        var filesToShare = [Any]()
        filesToShare.append(fileURL)
                
        let activityViewController = UIActivityViewController(activityItems: filesToShare, applicationActivities: nil)
                
        self.present(activityViewController, animated: true, completion: nil)
    }
}

extension ViewController: UIDocumentPickerDelegate {
    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        guard let selectedFileURL = urls.first else {
            return
        }
    
        restoreFromURL(selectedFileURL: selectedFileURL)
    }
}

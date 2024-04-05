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
import React

class ViewController: UIViewController {
  var bridge: RCTBridge!
  
  @IBOutlet weak var lblInfo: UILabel!
  @IBOutlet weak var btnCreate: UIButton!
  @IBOutlet weak var viewReactNative: UIView!
  
  private var account: Account!
  
  static var onCreateAccountCallback: (() -> Void)? = nil
  
  override func viewDidLoad() {
      super.viewDidLoad()
      // Do any additional setup after loading the view.
      
    viewReactNative.layer.borderWidth = 1
    viewReactNative.layer.borderColor = UIColor.red.cgColor
    
    
    btnCreate.addTarget(self, action: #selector(onButtonPressed(_:)), for: .touchUpInside)
    btnCreate.isHidden = true
    
    NotificationCenter.default.addObserver(self, selector: #selector(createAccount), name: Notification.Name("CreateAccount"), object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(livenessCheck), name: Notification.Name("LivenessCheck"), object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(getKeyValue), name: Notification.Name("GetKeyValue"), object: nil)

    
    account = Account.Builder()
        .withEnvironment(Environment.review)
        .withStoragePath("account1")
        .build()
    SelfSDKRNModule.account = account
    account.setDevMode(enabled: true)
    
    insertTestData()
  }
  
  private func insertTestData() {
      let data1 = KeyValue.Builder()
          .withKey("name")
          .withValue("Test User")
          .withSensitive(true)
          .build()
      account.store(keyValue: data1)
  }
  
  override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    openReactNative()
  }
    
  @objc func onButtonPressed(_ sender: Any) {
    let vc = LivenessCheckViewController()
    vc.account = self.account
    vc.onFinishCallback = {selfieImage, attestations in
        Task {

        }
    }
    self.present(vc, animated: true)
  }
  
  func openReactNative() {
    if let jsCodeLocation = RCTBundleURLProvider.sharedSettings().jsBundleURL(forBundleRoot: "index") {
      print("url: \(jsCodeLocation)")
      
      let mockData:NSDictionary = [:]
      
      let rootView = RCTRootView(
        bundleURL: jsCodeLocation,
        moduleName: "reactnative",
        initialProperties: mockData as [NSObject : AnyObject],
        launchOptions: nil
      )
      self.bridge = rootView.bridge
      
//      let vc = UIViewController()
//      vc.view = rootView
//      self.present(vc, animated: true, completion: nil)
      rootView.frame = viewReactNative.bounds
      viewReactNative.addSubview(rootView)
      
    }
    
  }
  
  @objc private func createAccount(notification: Notification) {
    log.debug("createAccount start")
    DispatchQueue.main.async {
      let vc = LivenessCheckViewController()
      vc.account = self.account
      vc.onFinishCallback = {selfieImage, attestations in
        Task {
            if !attestations.isEmpty {
              let selfId = try! await self.account.register(selfieImage: selfieImage, attestations: attestations)
              log.debug("SelfId: \(selfId)")
              NotificationCenter.default.post(name: Notification.Name("SelfIdUpdated"), object: nil, userInfo: ["selfId": selfId])
            }
        }
      }
      self.present(vc, animated: true)
    }
  }
  
  @objc private func livenessCheck(notification: Notification) {
    log.debug("livenessCheck start")
    DispatchQueue.main.async {
      let vc = LivenessCheckViewController()
      vc.account = self.account
      vc.onFinishCallback = {selfieImage, attestations in
        
      }
      self.present(vc, animated: true)
    }
  }
  
  @objc private func getKeyValue(notification: Notification) {
    log.debug("getKeyValue start")
    let callback = notification.userInfo?["callback"] as? RCTResponseSenderBlock
    if let key = notification.userInfo?["key"] as? String {
      DispatchQueue.main.async {
        let vc = LivenessCheckViewController()
        vc.account = self.account
        vc.onFinishCallback = {selfieImage, attestations in
          let value = self.account.get(key: key, attestations: attestations)
          log.debug("key-value \(value?.value())")
          callback?([value?.value()])
        }
        self.present(vc, animated: true)
      }
    }
  }
}

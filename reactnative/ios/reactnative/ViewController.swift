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
  
  override func viewDidLoad() {
      super.viewDidLoad()
      // Do any additional setup after loading the view.
      
    viewReactNative.layer.borderWidth = 1
    viewReactNative.layer.borderColor = UIColor.red.cgColor
    
    
    btnCreate.addTarget(self, action: #selector(onButtonPressed(_:)), for: .touchUpInside)
    
    account = Account.Builder()
        .withEnvironment(Environment.review)
        .withStoragePath("account1")
        .build()
    
  }
  
  override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    openReactNative()
  }
    
  @objc func onButtonPressed(_ sender: Any) {
    let vc = LivenessCheckViewController()
    vc.account = self.account
    vc.onFinishCallback = {selfieImage, attestation in
        Task {

        }
    }
//    let navigationController = UINavigationController(rootViewController: vc)

//    self.navigationController?.pushViewController(vc, animated: true)
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
}

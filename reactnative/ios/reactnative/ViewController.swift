//
//  ViewController.swift
//  self-ios-sample
//
//  Created by DO HAI VU on 06/09/2023.
//

import UIKit
import SwiftUI
//import self_ios_sdk
import UniformTypeIdentifiers
import CoreLocation
import React

class ViewController: UIViewController {
  var bridge: RCTBridge!
  
  @IBOutlet weak var lblInfo: UILabel!
  @IBOutlet weak var btnCreate: UIButton!
  @IBOutlet weak var viewReactNative: UIView!
  
  override func viewDidLoad() {
      super.viewDidLoad()
      // Do any additional setup after loading the view.
      
    
    viewReactNative.backgroundColor = .red
    
    
    btnCreate.addTarget(self, action: #selector(onButtonPressed(_:)), for: .touchUpInside)
  }
  
  override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    
  }
    
  @objc func onButtonPressed(_ sender: Any) {
    openReactNative()
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
      
      let vc = UIViewController()
      vc.view = rootView
      self.present(vc, animated: true, completion: nil)
//      viewReactNative.addSubview(rootView)
    }
    
  }
}

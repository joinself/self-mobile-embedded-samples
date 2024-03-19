//
//  SelfSDKRNModule.swift
//  reactnative
//
//  Created by DO HAI VU on 12/3/24.
//

import Foundation
import React
import self_ios_sdk
import CoreLocation

@objc(SelfSDKRNModule)
class SelfSDKRNModule: RCTEventEmitter  {
  override class func requiresMainQueueSetup() -> Bool {
    return false
  }
  
  var hasListeners = false
  static var account: Account? = nil
  
  override init() {
    super.init()
    print("SelfSDKRNModule init")
    
    NotificationCenter.default.addObserver(self, selector: #selector(selfIdUpdated), name: Notification.Name("SelfIdUpdated"), object: nil)
  }
  
  override func supportedEvents() -> [String]! {
    return ["EventSelfId"]
  }
  
  override func startObserving() {
    hasListeners = true
  }
  override func stopObserving() {
    hasListeners = false
  }
  
  
  func sendSelfIdEvent(_ selfId: String) {
    print("sendSelfIdEvent: \(hasListeners)")
    if (hasListeners) {
      self.sendEvent(withName: "EventSelfId", body: ["selfId": selfId])
    }
  }
  
  @objc private func selfIdUpdated(notification: Notification) {
    if let selfId = notification.userInfo?["selfId"] as? String {
      sendSelfIdEvent(selfId)
    }
  }
  
  @objc func createAccount(_ callback: RCTResponseSenderBlock) -> Void {
    NotificationCenter.default.post(name: Notification.Name("CreateAccount"), object: nil)
  }
  
  // get selfId from sdk
  @objc func getSelfId(_ callback: RCTResponseSenderBlock) -> Void {
    let selfId = SelfSDKRNModule.account?.identifier() ?? ""
    
    callback([selfId])
  }
  
  
  // get location from sdk
  @objc func getLocation(_ success: @escaping RCTResponseSenderBlock, error: @escaping RCTResponseSenderBlock) -> Void {
    let locationManager = CLLocationManager()
    if (locationManager.authorizationStatus == .notDetermined ||
        locationManager.authorizationStatus == .denied ||
        locationManager.authorizationStatus == .restricted) {
        locationManager.requestAlwaysAuthorization()
        locationManager.requestWhenInUseAuthorization()
        return
    }
    
    Task {
      if let locAttestation = try! await SelfSDKRNModule.account?.location() {
        print("Location fact: \(locAttestation.first?.fact())")
        if let fact = locAttestation.first?.fact() {
          success([fact.value()])
        }
      }
    }
  }
  
  
//  override func constantsToExport() -> [AnyHashable : Any]! {
//    return ["someKey": "someValue"]
//  }
  
}

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
  
  override func supportedEvents() -> [String]! {
    return ["EventSelfId"]
  }
  
  override func startObserving() {
    hasListeners = true
  }
  override func stopObserving() {
    hasListeners = false
  }
  
  
  func sendSelfIdEvent() {
    print("sendSelfIdEvent \(hasListeners)")
    if (hasListeners) {
      self.sendEvent(withName: "EventSelfId", body: ["selfId": "event1234567"])
    }
  }
  
  @objc func createAccount(_ callback: RCTResponseSenderBlock) -> Void {
    
    NotificationCenter.default.post(name: Notification.Name("CreateAccount"), object: nil)
    
    callback([""])
  }
  
  @objc func getSelfId(_ callback: RCTResponseSenderBlock) -> Void {
    let selfId = SelfSDKRNModule.account?.identifier() ?? ""
    
    callback([selfId])
  }
  
  
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
  
  
  override func constantsToExport() -> [AnyHashable : Any]! {
    return ["someKey": "someValue"]
  }
  
}

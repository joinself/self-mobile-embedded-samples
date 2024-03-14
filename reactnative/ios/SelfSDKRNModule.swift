//
//  SelfSDKRNModule.swift
//  reactnative
//
//  Created by DO HAI VU on 12/3/24.
//

import Foundation
import React

@objc(SelfSDKRNModule)
class SelfSDKRNModule: RCTEventEmitter  {
  override class func requiresMainQueueSetup() -> Bool {
    return false
  }
  
  var hasListeners = false
  
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
  
  @objc func getSelfId(_ callback: RCTResponseSenderBlock) -> Void {
    let selfId = "123"
    
    callback([selfId])
  }
  
  
  @objc func getLocation(_ success: RCTResponseSenderBlock, error: RCTResponseSenderBlock) -> Void {
    let data = "location"
    success([data])
    sendSelfIdEvent()
  }
  
  
  override func constantsToExport() -> [AnyHashable : Any]! {
    return ["someKey": "someValue"]
  }
  
}

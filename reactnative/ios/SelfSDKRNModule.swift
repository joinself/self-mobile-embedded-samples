//
//  SelfSDKRNModule.swift
//  reactnative
//
//  Created by DO HAI VU on 12/3/24.
//

import Foundation
import React

@objc(SelfSDKRNModule2)
class SelfSDKRNModule2: RCTEventEmitter  {
  var hasListeners = false
  
  @objc(callback:)
  func getSelfId(callback: RCTResponseSenderBlock) -> Void {
    let selfId = "12345"
    
    callback([selfId])
  }
  
//  @objc(getLocation:callback:)
//  func getLocation(callback: RCTResponseSenderBlock) -> Void {
//    let selfId = "12345"
//    callback([selfId])
//  }
  
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
    
    if (hasListeners) {
      self.sendEvent(withName: "EventSelfId", body: ["selfId": "1234567"])
    }
  }
//  @objc
//  func constantsToExport() -> [String: Any]! {
//    return ["someKey": "someValue"]
//  }
  
}

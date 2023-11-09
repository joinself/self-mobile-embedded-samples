//
//  SwiftUI_Host_AppApp.swift
//  SwiftUI Host App
//
//  Created by Long Pham on 08/11/2023.
//

import SwiftUI
import self_ios_sdk

@main
struct SwiftUI_Host_AppApp: App {
    
    init() {
        SelfSDK.initialize()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

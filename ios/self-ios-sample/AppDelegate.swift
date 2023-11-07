//
//  AppDelegate.swift
//  self-ios-sample
//
//  Created by DO HAI VU on 06/09/2023.
//

import UIKit
import SwiftyBeaver
import self_ios_sdk

let log = SwiftyBeaver.self

@main
class AppDelegate: UIResponder, UIApplicationDelegate {



    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        setupLogger()
        SelfSDK.initialize()
        
        return true
    }

    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }


    private func setupLogger() {
        let console = ConsoleDestination()  // log to Xcode Console
        console.format = "$N.$F:$l $L: $M $X"
        console.useNSLog = true
        console.asynchronously = false
        console.useTerminalColors = true
        
        let platform = BaseDestination()
        log.addDestination(platform)
        log.addDestination(console)
    }
}


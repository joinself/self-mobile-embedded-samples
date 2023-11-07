//
//  DialogHelper.swift
//  self-ios-sample
//
//  Created by DO HAI VU on 07/09/2023.
//

import Foundation
import UIKit

struct DialogHelper {
    static func getWindowScene() -> UIWindowScene? {
        return UIApplication.shared.connectedScenes.first as? UIWindowScene
    }
    
    static func getSceneDelegate() -> SceneDelegate? {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene, let sceneDelegate = windowScene.delegate as? SceneDelegate else {
            log.debug("Could not get scene delegate.")
            return nil}
        
        return sceneDelegate
    }
    
    static func getWindow() -> UIWindow? {
        guard let sceneDelegate = getSceneDelegate() else {
            return nil
        }
        
        guard let window = sceneDelegate.window else {
            log.debug("There is no window.")
            return nil
        }
        
        return window
    }
    
    static func getRootViewController() -> UIViewController? {
        let root = getWindow()?.rootViewController
        return root
    }
    
    var appDelegate: AppDelegate? {
        let appDelegate = UIApplication.shared.delegate as? AppDelegate
        return appDelegate
    }
    
    static func topViewController(controller: UIViewController? = getRootViewController()) -> UIViewController? {
        if let navigationController = controller as? UINavigationController {
            return topViewController(controller: navigationController.visibleViewController)
        }
        if let tabController = controller as? UITabBarController {
            if let selected = tabController.selectedViewController {
                return topViewController(controller: selected)
            }
        }
        if let presented = controller?.presentedViewController {
            return presented
        }
        return controller
    }
    
    
    static func showDialog(title: String = "", message: String,
                           negativeButtonTitle: String?,
                           positiveButtonTitle: String?,
                           cancelable: Bool = false,
                           completion: @escaping (Bool) -> Void) {
        
        DialogHelper.showActionsDialog(title: title, message: message, okButtonTitle: positiveButtonTitle, cancelButtonTitle: negativeButtonTitle, isVerticalButtons: false, cancelable: cancelable) {
            completion(true)
        } onCancel: {
            completion(false)
        }
    }
    
    static func showActionsDialog(title: String? = nil,
                                  message: String,
                                  okButtonTitle: String? = "button_ok".localized,
                                  cancelButtonTitle: String? = "button_cancel".localized,
                                  isVerticalButtons: Bool = true,
                                  cancelable: Bool = false,
                                  onAccept: @escaping () -> Void,
                                  onCancel: @escaping () -> Void) {
        
        guard let topViewController = topViewController() else {
            log.debug("Cant get the top view controller!")
            return
        }                
        
        let alertController = UIAlertController(title: title ?? "" , message: message, preferredStyle: .alert)
        
        if let okButtonTitle = okButtonTitle {
            let ok = UIAlertAction(title: okButtonTitle, style: .default) { action in
                onAccept()
            }
            
            alertController.addAction(ok)
        }
        
        if let cancelButtonTitle = cancelButtonTitle {
            let cancel = UIAlertAction(title: cancelButtonTitle, style: .cancel) { action in
                onCancel()
            }
            alertController.addAction(cancel)
        }
        
        topViewController.present(alertController, animated: true)
    }
}

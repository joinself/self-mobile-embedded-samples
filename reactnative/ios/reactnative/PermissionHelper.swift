//
//  PermissionHelper.swift
//  self-ios-sample
//
//  Created by DO HAI VU on 07/09/2023.
//

import Foundation
import UIKit
import AVFoundation
import Photos
import UserNotifications

class PermissionHelper {
    static let shared = PermissionHelper()
    private init() {
        NotificationCenter.default.addObserver(self, selector: #selector(appDidBecomeActive(_:)), name: UIApplication.didBecomeActiveNotification, object: nil)
    }
    
    deinit {
        
    }
    
    @objc func appDidBecomeActive(_ notification: NSNotification) {
        DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(1000)) {
            self.isRequesting = false
        }
    }
    
    var isRequesting = false
    
    func audioPermission(completion: @escaping (Bool, Bool, Error?) -> Void) { // success, isFirstTime
        let permissionStatus = AVAudioSession.sharedInstance().recordPermission
        switch permissionStatus {
        case AVAudioSession.RecordPermission.granted:
            completion(true, false, nil)
            
        case AVAudioSession.RecordPermission.undetermined:
            self.isRequesting = true
            AVAudioSession.sharedInstance().requestRecordPermission { success in
                DispatchQueue.main.async {
                    if success {
                        completion(true, true, nil)
                    } else {
                        completion(false, true, SelfError.CommonError.permissionDenied)
                    }
                }
            }
            
        case AVAudioSession.RecordPermission.denied:
            // notify user open settings
            DialogHelper.showDialog(message: "permission_audio_message".localized, negativeButtonTitle: "button_cancel".localized, positiveButtonTitle: "button_settings".localized) { ok in
                if (ok) {
                    UIApplication.shared.open(URL(string:UIApplication.openSettingsURLString)!, options: [:], completionHandler: { (success) in
                        
                        log.debug("Success = \(success)")
                        // TODO: Handle completion here
                        completion(false, false, SelfError.CommonError.permissionDenied)
                    })
                }
            }
            
        default:
            break
        }
    }
    
    static func hasCameraPermission() -> Bool {
        return AVCaptureDevice.authorizationStatus(for: .video) == .authorized
    }
    
    func cameraPermission(negativeButtonTitle: String? = "button_not_now".localized, message: String? = nil, completion: @escaping (Bool, Error?) -> Void) {
        let cameraAuthorizationStatus = AVCaptureDevice.authorizationStatus(for: .video)
        switch cameraAuthorizationStatus {
        case AVAuthorizationStatus.authorized:
            completion(true, nil)
            
        case AVAuthorizationStatus.notDetermined:
            self.isRequesting = true
            AVCaptureDevice.requestAccess(for: .video, completionHandler: { accessGranted in
                DispatchQueue.main.async {
                    if accessGranted {
                        completion(true, nil)
                    } else {
                        completion(false, SelfError.CommonError.permissionDenied)
                    }
                }
            })
            
        case AVAuthorizationStatus.denied:
            // notify user open settings
            var buttonCancelTitle: String? = "button_cancel".localized
            if negativeButtonTitle == nil {
                buttonCancelTitle = nil
            }
            DialogHelper.showDialog(message: "permission_camera_message".localized, negativeButtonTitle: buttonCancelTitle, positiveButtonTitle: "button_settings".localized) { ok in
                if (ok) {
                    UIApplication.shared.open(URL(string:UIApplication.openSettingsURLString)!, options: [:], completionHandler: { (success) in
                        
                        log.debug("Success = \(success)")
                        // TODO: Handle completion here
                        
                    })
                }
            }
            
        default:
            break
        }
    }
    
    func galleryPermission(completion: @escaping (Bool, Error?) -> Void) {
        let status = PHPhotoLibrary.authorizationStatus()
        switch status {
        case .notDetermined:
            self.isRequesting = true
            PHPhotoLibrary.requestAuthorization() { status in
                DispatchQueue.main.async {
                    if status == .authorized {
                        completion(true, nil)
                    } else {
                        completion(false, SelfError.CommonError.permissionDenied)
                    }
                }
            }
        case .denied, .restricted:
            // notify user open settings
            DialogHelper.showDialog(message: "permission_photo_message".localized, negativeButtonTitle: "button_cancel".localized, positiveButtonTitle: "button_settings".localized) { ok in
                if (ok) {
                    UIApplication.shared.open(URL(string:UIApplication.openSettingsURLString)!, options: [:], completionHandler: { (success) in
                        log.debug("Success = \(success)")
                    })
                }
            }
            completion(false, nil)
            
        case .authorized:
            completion(true, nil)
            
        default:
            break
        }
    }
}

//
//  LivenessCheckViewController.swift
//  self-ios
//
//  Created by SOTSYS216 on 22/04/21.
//  Copyright (c) 2021 All rights reserved.
//

import UIKit
import SwiftyBeaver
import self_ios_sdk

class LivenessCheckViewController: UIViewController {
    
    @IBOutlet weak var lblInfo: UILabel!
    @IBOutlet private weak var cameraView: UIView!
    
    @IBOutlet weak var lblCheckStatus: UILabel!
    
    private var livenessCheck = LivenessCheck()
    
    var account: Account!
    
    var onFinishCallback: ((Data, Attestation?) -> Void)? = nil
    
    override func viewDidLoad() {
        super.viewDidLoad()
                
        setupLivenessCheck()
        self.checkCameraPermissionAndSetupCameraSession()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
                
        
        if PermissionHelper.hasCameraPermission() {
//            if (!captureSession.isRunning) {
//                self.startSession()
//                self.setupUIAndStartChallenges()
//            }
        }
        
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
//        timeoutTimer?.invalidate()
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        
//        stopSession()
        livenessCheck.stop()
    }
    
    private func setupLivenessCheck() {
        livenessCheck.initialize(account: self.account, cameraView: self.cameraView)
        livenessCheck.onStatusUpdated = { status in
            self.updateCheckStatus(status: status)
        }
        livenessCheck.onChallengeChanged = { challege, error in
            self.updateUI(challenge: challege, error: error)
        }
        livenessCheck.onResult = { selfieImage, attestation in
            if self.onFinishCallback != nil {
                self.onFinishCallback?(selfieImage, attestation)
                self.onFinishCallback = nil
                self.navigationController?.popViewController(animated: true)
            }
            
        }
    }
    
//    private func setupUIAndStartChallenges() {
//        if PermissionHelper.hasCameraPermission() {
//            isTimeoutTimerStarted = false
//            setupChallenges()
//            delay(seconds: 3) {
//                self.captureImage = true
//                self.moveNextChallenge()
//            }
//        } else {
//            log.debug("Require camera permission.")
//        }
//    }
    
    private func checkCameraPermissionAndSetupCameraSession() {
        PermissionHelper.shared.cameraPermission(negativeButtonTitle: nil, message: "permission_camera_capture_selfie_message".localized) { success, error in
            if success {
                self.livenessCheck.start()
//                self.setupSession()
//                self.startSession()
//                self.setupUIAndStartChallenges()
            } else {
                // TODO: Display a snackbar message
                self.checkCameraPermissionAndSetupCameraSession()
            }
        }
    }
    
    
    private func updateCheckStatus(status: LivenessCheck.Status) {
        var msg = "status: "
        switch status {
        case .Passed:
            msg = msg + "passed"
        case .Error:
            msg = msg + "error"
        case .Info:
            msg = "status: info"
        }
        lblCheckStatus.text = msg
    }
    
    private func updateUI(challenge: LivenessCheck.Challenge, error: LivenessCheck.Error? = nil) {
        if let error = error {
            updateCheckStatus(status: .Error)
            switch error {
            case .FaceChanged:
                lblInfo.text = "liveness_out_of_preview".localized
//                timeoutTimer?.invalidate()
//                isTimeoutTimerStarted = false
//                setupChallenges()
//                self.delay(seconds: 2) {
//                    self.moveNextChallenge()
//                }
            case .OutOfPreview:
                lblInfo.text = "liveness_direction_message".localized
            }
            
            return
        }
        
        updateCheckStatus(status: .Info)
        switch challenge {
        case .None:
            break
        case .Done:
            updateCheckStatus(status: .Passed)
//            self.previewOverlayView.ovalBorder(color: .greenPrimary)
            lblInfo.text = "liveness_thank_you".localized
//            if (selfieImage == nil) {
//                isEnableDetecting = true
//                captureImageIfNot = true
//            }
//            ez.runThisAfterDelay(seconds: Double(3)) {
//                self.isEnableDetecting = false
////                self.moveToNextScreen()
//            }
        case .Smile:
            lblInfo.text = "liveness_smile".localized
        case .Blink:
            lblInfo.text = "liveness_blink".localized
        case .TurnLeft:
            lblInfo.text = "liveness_turn_left".localized
        case .TurnRight:
            lblInfo.text = "liveness_turn_right".localized
        case .LookUp:
            lblInfo.text = "liveness_look_up".localized
        }
        
    }
}

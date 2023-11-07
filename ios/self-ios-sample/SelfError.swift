//
//  SelfError.swift
//  self-ios-sample
//
//  Created by DO HAI VU on 07/09/2023.
//

import Foundation

struct SelfError {
    public enum CommonError: String, Error {
        case invalidJson
        case noActiveDevice
        case notfoundTopViewController
        case permissionDenied
        case askPermissionLater
        case missingSelfId
        case factIsVerified
        case phoneExisted
        
        // Backup & Restore
        case backupNotEnabled
        case noBackupFiles
        case invalidBackupFiles
        case encryptFailed
        case decryptFailed
        case invalidNonce
        case keyConversionFailed
        case pdsNotChanged
        case iCloudUnavailable
        case demoTokenInvalid
        case demoBackupFailed
        
        case connectionConnected
        case invitationExisted
        case mergeJsonError
        
        case noSender
        
        case createBearerTokenError
        
        case encodeFail
        case decodeFail
        case attestServiceNotSupported
        
        // PDS
        case pdsIsNotReady
        case pdsError
        case encryptionKeyNil
        
        case createDeviceIdError
        case deviceCheckError
        
        case missingSession
        
        case isProtectedDataNotAvailable
    }
}

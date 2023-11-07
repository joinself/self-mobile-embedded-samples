//
//  Extensions.swift
//  self-ios-sample
//
//  Created by DO HAI VU on 06/09/2023.
//

import Foundation
import UIKit

extension String {
    var localized: String {
        return NSLocalizedString(self, comment: "")
    }
}

extension Date {
    var unixTimestamp: Int64 {
        return Int64(timeIntervalSince1970 * 1_000)
    }
}

extension UIView {
}

extension CVImageBuffer {
    var cgImage: CGImage? {
        CVPixelBufferLockBaseAddress(self, .readOnly)
        
        let baseAddress = CVPixelBufferGetBaseAddress(self)
        let bytesPerRow = CVPixelBufferGetBytesPerRow(self)
        let (width, height) = (CVPixelBufferGetWidth(self), CVPixelBufferGetHeight(self))
        let bitmapInfo = CGBitmapInfo(rawValue: (CGBitmapInfo.byteOrder32Little.rawValue | CGImageAlphaInfo.premultipliedFirst.rawValue))
        let context = CGContext.init(data: baseAddress, width: width, height: height, bitsPerComponent: 8, bytesPerRow: bytesPerRow, space: CGColorSpaceCreateDeviceRGB(), bitmapInfo: bitmapInfo.rawValue)
        
        guard let cgImage = context?.makeImage() else {
            return nil
        }
        
        CVPixelBufferUnlockBaseAddress(self, .readOnly)
        
        return cgImage
    }
}


extension UIViewController {
    
    class var storyboardID: String {
        return "\(self)"
    }
    
    static func instantiate(from: AppStoryboard) -> Self {
        return from.viewController(viewControllerClass: self)
    }
}

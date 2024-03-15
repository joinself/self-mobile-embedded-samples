//
//  LivenessCheckViewController.swift
//  reactnative
//
//  Created by DO HAI VU on 14/3/24.
//

import Foundation
import UIKit
import SwiftyBeaver

class LivenessCheckViewController: UIViewController {
  
  private var descLabel: UILabel!
  private var cameraView: UIView!
  
  override func loadView() {
    view = UIView()
    view.backgroundColor = .white
    
    let stackView = UIStackView()
    stackView.translatesAutoresizingMaskIntoConstraints = false
    stackView.axis = .vertical
    stackView.distribution = .equalSpacing
    stackView.alignment = .center
    stackView.spacing = 20.0
    view.addSubview(stackView)
    
    NSLayoutConstraint.activate([
      stackView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
      stackView.centerYAnchor.constraint(equalTo: view.centerYAnchor)
    ])
    
    descLabel = UILabel()
    descLabel.translatesAutoresizingMaskIntoConstraints = false
    descLabel.text = "hello"
    stackView.addArrangedSubview(descLabel)
    
    cameraView = UIView(frame: CGRect(x: 0, y: 0, width: 300, height: 300))
    cameraView.translatesAutoresizingMaskIntoConstraints = false
    cameraView.backgroundColor = .lightGray
    cameraView.heightAnchor.constraint(equalToConstant: 300).isActive = true
    cameraView.widthAnchor.constraint(equalToConstant: 300).isActive = true
    
    stackView.addArrangedSubview(cameraView)
  }
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    log.debug("LivenessCheckViewController viewDidLoad")
  }
}

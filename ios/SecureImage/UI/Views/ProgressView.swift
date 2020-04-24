//
// SecureImage
//
// Copyright © 2018 Province of British Columbia
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// Created by Jason Leach on 2018-01-17.
//

import Foundation
import UIKit

class ProgressView: UIView {
    
    @IBOutlet weak var progressLabel: UILabel!
    @IBOutlet weak var titleLabel: UILabel!
    
    private var size : CGFloat {
        get {
            return min(self.bounds.height, self.bounds.width)
        }
    }
    private var progressLayer: CAShapeLayer?
    private var animationTimer: Timer?
    private var progress: CGFloat = 0.0
    private let startAngle = CGFloat(3 * Double.pi / 2.0)
    private let endAngle = CGFloat(Double.pi * 3 + Double.pi / 2)
    private let lineWidth: CGFloat = 9.0
    private let animationDuration = 0.33
    private let radiusOffset: CGFloat = 50.0
    
    override func awakeFromNib() {
        
        super.awakeFromNib()
        
        commonInit()
    }
    
    override func layoutSubviews() {

        super.layoutSubviews()

        createProgressLayer()
    }

    private func commonInit() {
    
        progressLabel.textColor = UIColor.white
        titleLabel.textColor = UIColor.white
    }
    
    internal func animateProgressViewToProgress(_ progress: Float) {
        
        self.progress = CGFloat(progress)

        if let progressLayer = progressLayer {
            let animation = CABasicAnimation(keyPath: "strokeEnd")
            animation.duration = animationDuration
            animation.fromValue = CGFloat(progressLayer.strokeEnd)
            animation.toValue = CGFloat(progress)
            animation.fillMode = kCAFillModeForwards
            progressLayer.strokeEnd = CGFloat(progress)
            progressLayer.add(animation, forKey: "animation")
            
            // Update other UI elements conjunction with the animation timer
            animationTimer = Timer.scheduledTimer(timeInterval: animationDuration, target: self, selector: #selector(ProgressView.animationTimerTick(_:)), userInfo: nil, repeats: false)
        }
    }
    
    @objc internal func animationTimerTick(_ sender: Timer) {

        progressLabel.text = "\(String(format:"%0.0f", progress * 100))%"
    }
    
    private func createProgressLayer() {

        // Dont use `center` because it isn't where we think it should because
        // we're not initalized with a frame.
        let centerPoint = progressLabel.center

        if progressLayer != nil {
            progressLayer!.removeFromSuperlayer()
        }
        
        progressLayer = CAShapeLayer()
        
        if let progressLayer = progressLayer {
            progressLayer.path = UIBezierPath(arcCenter: centerPoint, radius: size / 2.0 - radiusOffset, startAngle: startAngle, endAngle: endAngle, clockwise: true).cgPath
            progressLayer.backgroundColor = UIColor.blue.cgColor
            progressLayer.fillColor = UIColor.clear.cgColor
            progressLayer.lineWidth = lineWidth
            progressLayer.strokeColor = UIColor.white.cgColor
            progressLayer.strokeStart = 0.0
            progressLayer.strokeEnd = progress
            
            layer.addSublayer(progressLayer)
        }
    }
}

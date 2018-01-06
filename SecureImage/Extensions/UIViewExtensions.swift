//
// SecureImage
//
// Copyright © 2017 Province of British Columbia
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
// Created by Jason Leach on 2017-12-22.
//

import Foundation
import UIKit

let percentageOfViewForCircleStrokeWidht: CGFloat = 0.20

extension UIView {

    private func circularMaskLayer() -> CAShapeLayer {
        
        let width = bounds.size.width
        let height = bounds.size.height
        
        let mask = CAShapeLayer()
        mask.bounds = CGRect(x: 0, y: 0, width: width, height: height)
        mask.position = CGPoint(x: width/2, y: height/2)
        mask.path = UIBezierPath.init(ovalIn: CGRect(x: 0, y: 0, width: width, height: height)).cgPath
        
        return mask
    }
    
    private func circleStrokeWith() -> CGFloat {
        
        return min(bounds.size.width, bounds.size.height) * percentageOfViewForCircleStrokeWidht
    }
    
    func createMaskLayer(_ fillColor: UIColor = UIColor.black) {
        
        let mask = circularMaskLayer()
    
        mask.fillColor = fillColor.cgColor
        mask.fillRule = kCAFillRuleEvenOdd
        
        layer.mask = mask
    }
    
    func createCircleLayer(_ strokeColor: UIColor) {
       
        let mask = circularMaskLayer()

        mask.fillColor = UIColor.clear.cgColor
        mask.strokeColor = strokeColor.cgColor
        mask.lineWidth = circleStrokeWith()
        
        layer.addSublayer(mask)
    }
}

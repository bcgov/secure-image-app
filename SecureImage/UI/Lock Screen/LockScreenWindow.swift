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
// Created by Jason Leach on 2017-12-13.
//

import Foundation
import UIKit

class LockScreenWindow: UIWindow {
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)

        commonInit()
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)

        commonInit()
    }
    
    private func commonInit() {
       windowLevel = 1  // We always go on top !
    }
    
    public func show() {
        rootViewController = LockScreenAuthenticateViewController.viewController()
        alpha = 1.0
        makeKeyAndVisible()
    }
    
    public func hide(animated: Bool = true, completion: (() -> Void)?) {
        let animationDuration: Double = 0.25

        UIView.animate(withDuration: animated ? animationDuration : 0.0, delay: 0.0, options: .curveEaseInOut, animations: { [weak self] in
            self?.alpha = 0.0
        }, completion: { _ in
            completion?()
        })
    }
}

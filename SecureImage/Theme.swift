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
// Created by Jason Leach on 2018-01-08.
//

import Foundation
import UIKit
import AVKit


class Theme {
    
    static let preferredStatusBarStyle: UIStatusBarStyle = UIStatusBarStyle.lightContent

    internal class func apply() {

        styleNavbar()
    }
        
    private class func styleNavbar() {
        
        UINavigationBar.appearance().barTintColor = UIColor.governmentDarkBlue()
        UINavigationBar.appearance().tintColor = .white
        UINavigationBar.appearance().titleTextAttributes = [NSAttributedStringKey.foregroundColor: UIColor.white]
        UINavigationBar.appearance().isTranslucent = false
    }
}

extension UIColor {
    
    class func governmentDarkBlue() -> UIColor {
        return UIColor(red:0.12, green:0.21, blue:0.42, alpha:1)
    }
    
    class func governmentDeepYellow() -> UIColor {
        return UIColor(red:0.96, green:0.66, blue:0.11, alpha:1)
    }
    
    class func albumOverlayBlue() -> UIColor {
        return UIColor(red:0, green:0.2, blue:0.4, alpha:0.8)
    }
    
    class func blueText() -> UIColor {
        return UIColor(red:0, green:0.2, blue:0.4, alpha:1)
    }
    
    class func disabledButtonBlue() -> UIColor {
        return UIColor(red:0.33, green:0.46, blue:0.65, alpha:1)
    }
    
    class func alertRed() -> UIColor {
        return UIColor(red:0.82, green:0.01, blue:0.11, alpha:1)
    }
    
    class func lightGreyBorder() -> UIColor {
        return UIColor(red:0.59, green:0.59, blue:0.59, alpha:1)
    }
}

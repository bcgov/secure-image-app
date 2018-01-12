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
// Created by Jason Leach on 2018-01-11.
//

import Foundation
import Alamofire

class NetworkManager {
    
    // singelton
    static let shared = NetworkManager()

    private static let host = "www.google.com"
    private let reachabilityManager: NetworkReachabilityManager? = Alamofire.NetworkReachabilityManager(host: NetworkManager.host)
    internal var isReachableOnEthernetOrWiFi: Bool = false
    internal var onReacabilityChanged: ((_ isReachableOnEthernetOrWiFi: Bool) -> ())?
    
    internal func start() {

        reachabilityManager?.listener = { status in
            
            if let manager = self.reachabilityManager {
                self.isReachableOnEthernetOrWiFi = manager.isReachableOnEthernetOrWiFi
            }

            switch status {
                
            case .notReachable:
                self.handleWiFiUnavailable()
                
            case .unknown :
                self.handleWiFiUnavailable()

            case .reachable(.ethernetOrWiFi):
                self.handleWiFiAvailable()

            case .reachable(.wwan):
                self.handleWiFiUnavailable()
            }
        }

        reachabilityManager?.startListening()
    }

    private func handleWiFiUnavailable() {
        
        onReacabilityChanged?(isReachableOnEthernetOrWiFi)
        NotificationCenter.default.post(name: Notification.Name.wifiAvailabilityChanged, object: nil)
    }
    
    private func handleWiFiAvailable() {
        
        onReacabilityChanged?(isReachableOnEthernetOrWiFi)
        NotificationCenter.default.post(name: Notification.Name.wifiAvailabilityChanged, object: nil)
    }
}

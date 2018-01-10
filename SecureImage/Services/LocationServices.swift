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
// Created by Jason Leach on 2018-01-10.
//

import Foundation
import CoreLocation
import RealmSwift

class LocationServices: NSObject {
    
    private let locationManager: CLLocationManager = {
        let lm = CLLocationManager()
        lm.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
        
        return lm
    }()
    
    private var lastKnowCoordinate: CLLocationCoordinate2D?

    internal func addLocation(to document: Document) {
        
        guard let realm = try? Realm(), let loc = lastKnowCoordinate else {
            return
        }
        
        if let doc = realm.objects(Document.self).filter("id == %@", document.id).first {
            do {
                try realm.write {
                    doc.latitude = loc.latitude
                    doc.longitude = loc.longitude
                }
            } catch {
                print("Unable to update document with coordinates")
            }
            
            return
        }
        
        document.latitude = loc.latitude
        document.longitude = loc.longitude
    }

    internal func start() {
        
        locationManager.requestWhenInUseAuthorization()
        
        if CLLocationManager.locationServicesEnabled() {
            switch CLLocationManager.authorizationStatus() {
            case .notDetermined, .restricted, .denied:
                ()
            case .authorizedAlways, .authorizedWhenInUse:
                locationManager.delegate = self
                locationManager.startUpdatingLocation()
            }
        } else {
            print("WARNING: Location services are not enabled")
        }
    }
    
    internal func stop() {
        
        locationManager.stopUpdatingLocation()
    }
}

extension LocationServices: CLLocationManagerDelegate {
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        
        guard let loc = manager.location else {
            return
        }
        
        lastKnowCoordinate = loc.coordinate
    }
    
    // Automatically paused
    func locationManagerDidPauseLocationUpdates(_ manager: CLLocationManager) {
        
        // When paused we may drift far from our location and it could take some time
        // before we get a new location. Best to clear the last know location because it may
        // be wildly inaccurate.
        
        lastKnowCoordinate = nil
    }
}

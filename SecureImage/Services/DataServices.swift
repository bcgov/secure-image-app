//
// SecureImage
//
// Copyright © 2017 Province of British Columbia
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
// Created by Jason Leach on 2017-12-14.
//

import UIKit
import RealmSwift
import SwiftKeychainWrapper

class DataServices: NSObject {
    
    static let shared = DataServices()
    static let realmFileName = "default.realm"
    static let temporaryRealmName = "temporary.realm"
    
    // MARK: Maintenance
    
    override init() {
        
        DataServices.configureRealm()
        DataServices.compactRealm()
    }
    
    internal class func configureRealm() {
        
        let config = Realm.Configuration(fileURL: DataServices.realmPath(),
                                         schemaVersion: 0,
                                         migrationBlock: { migration, oldSchemaVersion in
                                            // check oldSchemaVersion here, if we're newer call
                                            // a method(s) specifically designed to migrate to
                                            // the desired schema. ie `self.migrateSchemaV0toV1(migration)`
        })
        
        Realm.Configuration.defaultConfiguration = config
    }
    
    // Allow customization of the Realm; this will let us keep it in a location that is not
    // backed up if needed.
    private class func realmPath() -> URL {
        
        guard let workspace = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true).first else {
            fatalError("Unable to determine the applications working directory")
        }
        
        var workspaceURL = URL(fileURLWithPath: workspace, isDirectory: true).appendingPathComponent("db")
        var directory: ObjCBool = ObjCBool(false)
        
        if !FileManager.default.fileExists(atPath: workspaceURL.path, isDirectory: &directory) {
            // no backups
            var resourceValues = URLResourceValues()
            resourceValues.isExcludedFromBackup = true
            
            do  {
                try FileManager.default.createDirectory(at: workspaceURL, withIntermediateDirectories: false, attributes: nil)
                try workspaceURL.setResourceValues(resourceValues)
            } catch {
                fatalError("Unable to create a location to store the database")
            }
        }
        
        return URL(fileURLWithPath: realmFileName, isDirectory: false, relativeTo: workspaceURL)
    }
    
    private class func compactRealm() {
        
        // This will be important because we're storing large amounts of data in the db
        
        let defaultRealmPathURL = realmPath().deletingLastPathComponent() // remove file name
        let temporaryRealmPathURL  = defaultRealmPathURL.appendingPathComponent(temporaryRealmName, isDirectory: false)
        
        if !FileManager.default.fileExists(atPath: temporaryRealmPathURL.path) {
            return
        }
        
        // Compact Realm in an autorelease pool to prevent singletons from becoming persistent
        autoreleasepool {
            do {
                // Cleanup any old work
                if FileManager.default.fileExists(atPath: temporaryRealmPathURL.path) {
                    try FileManager.default.removeItem(at: temporaryRealmPathURL)
                }
                
                // Copying the realm will recover lost space
                try Realm().writeCopy(toFile: temporaryRealmPathURL)
                try FileManager.default.removeItem(at: defaultRealmPathURL)
                try FileManager.default.moveItem(atPath: temporaryRealmPathURL.path, toPath: defaultRealmPathURL.path)
            } catch {
                fatalError("Unable to compact Realm")
            }
        }
    }
}


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
    
    static let realmFileName = "default.realm"
    
    // MARK: Management

    internal class func setup() {

        DataServices.configureRealm()
//        DataServices.seed()
    }
    
    private class func realmEncryptionKey() -> Data? {

        if let key = KeychainWrapper.standard.string(forKey: Constants.Keychain.RealmEncryptionKey), let data = Data(base64Encoded: key) {
            return data
        }
        
        var key = Data(count: 64)
        key.withUnsafeMutableBytes { (keyByteArray: UnsafeMutablePointer<UInt8>) -> Void in
            let status = SecRandomCopyBytes(kSecRandomDefault, key.count, keyByteArray)
            if status != errSecSuccess {
                fatalError("Unable to extract randome bytes for the encryption key")
            }

            key = Data.init(bytes: keyByteArray, count: key.count)
            
            // Securley store they key
            guard KeychainWrapper.standard.set(key.base64EncodedString(), forKey: Constants.Keychain.RealmEncryptionKey) else {
                fatalError("Unalbe to store the Realm encryption key")
            }
        }
        
        if key.count == 0 {
            print("WARNING: The Realm encryption key is empty !!!")
        }

        return key.count == 0 ? nil : key
    }
    
    private class func configureRealm() {
        
        let config = Realm.Configuration(fileURL: DataServices.realmPath(),
                                         encryptionKey: DataServices.realmEncryptionKey(),
                                         schemaVersion: 0,
                                         migrationBlock: { migration, oldSchemaVersion in
                                            // check oldSchemaVersion here, if we're newer call
                                            // a method(s) specifically designed to migrate to
                                            // the desired schema. ie `self.migrateSchemaV0toV1(migration)`
                                            if (oldSchemaVersion < 1) {
                                                // Nothing to do. Realm will automatically remove and add fields
                                            }
        },
                                         shouldCompactOnLaunch: { totalBytes, usedBytes in
                                            // totalBytes refers to the size of the file on disk in bytes (data + free space)
                                            // usedBytes refers to the number of bytes used by data in the file
                                            
                                            // Compact if the file is over 100MB in size and less than 50% 'used'
                                            let oneHundredMB = 100 * 1024 * 1024
                                            return (totalBytes > oneHundredMB) && (Double(usedBytes) / Double(totalBytes)) < 0.5
        })
        
        Realm.Configuration.defaultConfiguration = config
    }
    
    // Allow customization of the Realm; this will let us keep it in a location that is not
    // backed up if needed.
    private class func realmPath() -> URL {
        
        var workspaceURL = URL(fileURLWithPath: DataServices.documentsURL().path, isDirectory: true).appendingPathComponent("db")
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
    
    public class func documentsURL() -> URL {
        return FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
    }

    // This is meant for the simulator only for developer testing.
    #if (arch(i386) || arch(x86_64)) && os(iOS) && DEBUG
    internal class func seed() {
        // This is for testing only !!!
        do {
            let realm = try Realm()
            let files = ["IMG_0093.jpg", "IMG_0094.jpg", "IMG_0095.jpg", "IMG_0096.jpg", "IMG_0097.jpg", "IMG_0098.jpg",
                         "IMG_0093.jpg", "IMG_0094.jpg", "IMG_0095.jpg", "IMG_0096.jpg", "IMG_0097.jpg", "IMG_0098.jpg",
                         "IMG_0093.jpg", "IMG_0094.jpg", "IMG_0095.jpg", "IMG_0096.jpg", "IMG_0097.jpg", "IMG_0098.jpg",
                         "IMG_0093.jpg", "IMG_0094.jpg", "IMG_0095.jpg", "IMG_0096.jpg", "IMG_0097.jpg", "IMG_0098.jpg"]
            let album = Album()
            
            for file in files {
                if let image = UIImage(named: file, in: Bundle(for: DataServices.self), compatibleWith: nil), let imageData = UIImageJPEGRepresentation(image, CGFloat(Constants.Defaults.jPEGCompressionRatio)) {
                    
                    let doc = Document()
                    doc.id = UUID().uuidString
                    doc.imageData = imageData
                    doc.createdAt = Date()
                    doc.modifiedAt = Date()

                    album.documents.append(doc)
                }
            }

            try realm.write {
                realm.add(album)
            }
        }  catch {
            fatalError("Unable to seed Realm")
        }
    }
    #endif
    
    // MARK: Migration
    
    private func f() {
        
    }
    
    // MARK: Helpers

    internal class func add(image: Data, to album: Album) {

        print("image size = \(ByteCountFormatter.string(fromByteCount: Int64(image.count), countStyle: .file))")
        
        guard let realm = try? Realm() else {
            print("Unable open realm")
            return
        }
        
        if let myAlbum = realm.objects(Album.self).filter("id == %@", album.id).first {
            let doc = Document()
            doc.id = UUID().uuidString
            doc.imageData = image
            doc.createdAt = Date()
            doc.modifiedAt = Date()
            
            do {
                try realm.write {
                    myAlbum.documents.append(doc)
                }
            } catch {
                fatalError("Unable to write to realm")
            }
            
            return
        }
        
        return
    }
    
    internal class func remove(album: Album) -> Bool {

        guard let realm = try? Realm() else {
            print("Unable open realm")
            return false
        }
        
        if let myAlbum = realm.objects(Album.self).filter("id == %@", album.id).first {
            do {
                try realm.write {
                    realm.delete(myAlbum);
                }
                return true
            } catch {
                fatalError("Unable to write to realm")
            }
        }
        
        return false
    }
    
    internal class func canAddToAlbum(album: Album) -> Bool {
        
        guard let realm = try? Realm() else {
            print("Unable open realm")
            return false
        }
        
        if let myAlbum = realm.objects(Album.self).filter("id == %@", album.id).first {
            if myAlbum.documents.count >= Constants.Defaults.maxAlbumImageCount {
                return false
            }
        }
        
        return true
    }
}

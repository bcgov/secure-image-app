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
import SingleSignOn

class BackendAPI {
 
    class func createAlbum(credentials: Credentials, completionHandler: @escaping (_ remoteAlbumId: String?) -> ()) {

        guard let endpoint = URL(string: Constants.API.createAlbumPath, relativeTo: Constants.API.serverURL!) else {
            return
        }

        let headers = ["Authorization": "Bearer \(credentials.accessToken)"]
        
        Alamofire.request(endpoint, method: .post, parameters: nil, encoding: URLEncoding.default, headers: headers)
            .responseJSON { response in
                guard response.result.isSuccess else {
                    completionHandler(nil)
                    return
                }
                
                guard let json = response.result.value as? [String: Any] else {
                    print("No JSON returned in response.")
                    print("Error: \(String(describing: response.result.error))")
                    
                    return
                }
                
                if let status = json["success"] as? Bool, status == false {
                    print("The request failed.")
                    print("Error: \(String(describing: json["error"] as? String ?? "No message provided"))")
                }
                
                if let albumId = json["id"] as? String {
                    completionHandler(albumId)
                    return
                }
                
                completionHandler(nil)
        }
    }
    
    class func add(credentials: Credentials, image: Data, toRemoteAlbum albumId: String, completionHandler: @escaping (_ remoteDocumentId: String?) -> ()) {
        
        let pathKey = ":id"
        let path = Constants.API.addPhotoToAlbumPath.replacingOccurrences(of: pathKey, with: albumId, options: .literal, range: nil)

        guard let endpoint = URL(string: path, relativeTo: Constants.API.serverURL!) else {
            return
        }
        
        let headers: HTTPHeaders = [
            "Authorization": "Bearer \(credentials.accessToken)",
            "Content-type": "multipart/form-data"
        ]
        
        Alamofire.upload(multipartFormData: { (multipartFormData) in
            //  for (key, value) in parameters {
            //      multipartFormData.append("\(value)".data(using: String.Encoding.utf8)!, withName: key as String)
            //  }
            
            multipartFormData.append(image, withName: "file", fileName: "image.jpg", mimeType: "image/jpeg")
            
        }, usingThreshold: UInt64.init(), to: endpoint, method: .post, headers: headers) { (result) in
            switch result {
            case .success(let upload, _, _):
                upload.responseJSON { response in
                    print("Succesfully uploaded")
   
                    guard let json = response.result.value as? [String: Any] else {
                        print("No JSON returned in response.")
                        print("Error: \(String(describing: response.result.error))")
                        
                        completionHandler(nil)
                        
                        return
                    }
                    
                    if let albumId = json["id"] as? String {
                        completionHandler(albumId)
                        return
                    }
                    
                    completionHandler(nil)
                }
            case .failure(let error):
                print("ERROR: Uploading image, \(error.localizedDescription)")
            }
        }
    }
    
    class func package(credentials: Credentials, remoteAlbumId: String, completionHandler: @escaping (_ albumDownloadUrl: URL?) -> ()) {
        
        let pathKey = ":id"
        let path = Constants.API.addPhotoToAlbumPath.replacingOccurrences(of: pathKey, with: remoteAlbumId, options: .literal, range: nil)

        guard let endpoint = URL(string: path, relativeTo: Constants.API.serverURL!) else {
            return
        }
        
        let headers: HTTPHeaders = [
            "Authorization": "Bearer \(credentials.accessToken)"
        ]
        
        Alamofire.request(endpoint, method: .get, parameters: nil, encoding: URLEncoding.default, headers: headers)
            .responseJSON { response in
                guard response.result.isSuccess else {
                    completionHandler(nil)
                    return
                }
                
                guard let json = response.result.value as? [String: Any] else {
                    print("No JSON returned in response.")
                    print("Error: \(String(describing: response.result.error))")
                    
                    completionHandler(nil)
                    
                    return
                }
                
                if let urlAsString = json["url"] as? String, let url = URL(string: urlAsString) {
                    completionHandler(url)
                    return
                }
                
                completionHandler(nil)
        }
        
    }
}

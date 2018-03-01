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

struct Constants {

    struct Defaults {
        static let jPEGCompressionRatio = 0.5
        static let dateFormat = "YYYY-MM-dd\'T\'HH:mm"
        static let maxAlbumImageCount = 100
    }
    
    struct Keychain {
        static let RealmEncryptionKey = "RealmEncryptionKey"
    }
    
    struct SSO {
        static let baseUrl = URL(string: "https://dev-sso.pathfinder.gov.bc.ca")!
        static let redirectUri = "bcgov://android"
        static let clientId = "secure-image"
        static let realmName = "mobile"
        static let idpHint = "idir"
    }
    
    struct Album {
        // When converted to cammelcase these must match the related `Album`
        // model properties.
        static let Fields = [(name: "Album Name", placeHolderText: "Jane Doe"),
                             (name: "Comment", placeHolderText: "Your text here")]
    }
    
    struct API {
        static let serverURL = URL(string: "http://api-devex-mpf-secure-dev.pathfinder.gov.bc.ca/v1/")
        static let createAlbumPath = "album/"
        static let addPhotoToAlbumPath = "album/:id"
        static let getAlbumDownloadUrlPath = "album/:id"
        static let addFieldNotesToAlbumPath = "album/:id/note"
    }
}

extension Notification.Name {
    static let userAuthenticated = Notification.Name("userAuthenticated")
    static let wifiAvailabilityChanged = Notification.Name("wifiAvailabilityChanged")
}

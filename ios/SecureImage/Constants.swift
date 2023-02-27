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
        static let prodUri = "https://loginproxy.gov.bc.ca"
        static let devUri = "https://dev.loginproxy.gov.bc.ca"
        
        static let baseUrl = URL(string: prodUri)!
        static let redirectUri = "secure-image://client"
        static let clientId = "secure-image-3950"
        static let realmName = "standard"
        static let idpHint = "idir"
    }
    
    struct Album {
        // When converted to camelcase these must match the related `Album`
        // model properties.
        static let ExpirationInDays = 7
        static let Fields = [(name: "Album Name", placeHolderText: "Jane Doe"),
                             (name: "Comment", placeHolderText: "Your text here")]
    }
    
    struct API {
        static let prodUri = "https://secure-image.apps.gov.bc.ca/v1/"
        static let devUri = "https://secure-image-dev.apps.gov.bc.ca/v1/"
        
        static let serverURL = URL(string: prodUri)
        static let createAlbumPath = "album/"
        static let addPhotoToAlbumPath = "album/:id"
        static let getAlbumDownloadUrlPath = "album/:id"
        static let addFieldNotesToAlbumPath = "album/:id/note"
    }
    
    struct Email {
        struct Clients {
            static let yahooMailBaseURI = "ymail://mail/compose"
            static let outlookBaseURI = "ms-outlook://compose"
            static let gmailBaseURI = "googlegmail://co"
        }
    }
}

extension Notification.Name {
    static let userAuthenticated = Notification.Name("userAuthenticated")
    static let wifiAvailabilityChanged = Notification.Name("wifiAvailabilityChanged")
}

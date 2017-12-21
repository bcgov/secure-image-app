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
    }
    
    struct Keychain {
        static let RealmEncryptionKey = "RealmEncryptionKey"
    }
    
    struct Album {
        // When converted to cammelcase these must match the related `Album`
        // model properties.
        static let Fields = [(name: "Album Name", placeHolderText: "Jane Doe"),
                             (name: "Case Number", placeHolderText: "JCL12345"),
                             (name: "Address", placeHolderText: "1965 Blue St"),
                             (name: "Comments", placeHolderText: "Your text here")]
    }
}

extension Notification.Name {
    static let userAuthenticated = Notification.Name("userAuthenticated")
}

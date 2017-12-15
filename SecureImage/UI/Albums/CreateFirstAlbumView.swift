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

import UIKit

typealias CreateFirstAlbumTouched = () -> Void

class CreateFirstAlbumView: UIView {

    private let tapGestureRecognizer: UITapGestureRecognizer = UITapGestureRecognizer()
    public var onCreateFirstAlbumTouched: CreateFirstAlbumTouched?
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        commonInit()
    }
    
    private func commonInit() {
        tapGestureRecognizer.numberOfTapsRequired = 1
        tapGestureRecognizer.addTarget(self, action: #selector(self.tapDetected(gestureRecognizer:)))

        addGestureRecognizer(tapGestureRecognizer)
    }
    
    @objc
    private dynamic func tapDetected(gestureRecognizer: UITapGestureRecognizer) {
        onCreateFirstAlbumTouched?()
    }
}

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
// Created by Jason Leach on 2017-12-18.
//

import UIKit

typealias UploadAlbumCallback = () -> Void
typealias SaveAlbumCallback = () -> Void
typealias ViewAllCallback = () -> Void

class FuncitonTableViewCell: UITableViewCell {

    internal var onUploadAlbumTouched: UploadAlbumCallback?
    internal var onSaveAlbumTouched: SaveAlbumCallback?
    internal var onViewAllImagesTouched: ViewAllCallback?
    
    override func awakeFromNib() {
        super.awakeFromNib()
    }
    
    @IBAction dynamic private func uploadAlbumTouched(sender: UIButton) {
        onUploadAlbumTouched?()
    }
    
    @IBAction dynamic private func saveAlbumTouched(sender: UIButton) {
        onSaveAlbumTouched?()
    }
    
    @IBAction dynamic private func viewAllImagesTouched(sender: UIButton) {
        onViewAllImagesTouched?()
    }
}

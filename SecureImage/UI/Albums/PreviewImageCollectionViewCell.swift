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
import QuartzCore
import RealmSwift

typealias DeleteImageCallback = (_ document: Document) -> Void

class PreviewImageCollectionViewCell: UICollectionViewCell {
    
    @IBOutlet weak var thumbnailImageView: UIImageView!
    @IBOutlet weak var deleteButton: UIButton!
    
    private var layoutComplete: Bool = false
    internal var document: Document? {
        didSet {
            if let data = document?.imageData {
                self.thumbnailImageView.image = UIImage(data: data)
            }
        }
    }
    internal var onDeleteImageTouched: DeleteImageCallback?
    
    override func awakeFromNib() {
        
        super.awakeFromNib()

        commonInit()
    }
    
//    override func layoutSubviews() {
//
//        super.layoutSubviews()
//    
//    }

    private func commonInit() {

        deleteButton.layer.cornerRadius = deleteButton.bounds.size.height / 2.0
        deleteButton.clipsToBounds = true
        deleteButton.backgroundColor = UIColor.blue
    }
    
    @IBAction dynamic private func deleteImageTouched(_ sender: UIButton) {

        guard let document = document else {
            fatalError("The document should be set at this point")
        }

        onDeleteImageTouched?(document)
    }
}

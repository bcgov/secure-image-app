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
import RealmSwift

typealias DeleteImageCallback = (_ document: Document) -> Void

class PreviewImageCollectionViewCell: UICollectionViewCell {
    
    @IBOutlet weak var thumbnailImageView: UIImageView!
    @IBOutlet weak var deleteButton: UIButton!
    @IBOutlet weak var multiSelectIndicatorView: UIView!
    @IBOutlet var deleteButtonWidthConstraint: NSLayoutConstraint!
    @IBOutlet var deleteButtonWidthToZeroConstraint: NSLayoutConstraint!
    
    private var maskLayerNeedsSet = true
    internal var onDeleteImageTouched: DeleteImageCallback? {
        didSet {
            deleteButtonWidthConstraint.isActive = true
            deleteButtonWidthToZeroConstraint.isActive = false
            
            setNeedsLayout()
        }
    }
    internal var multiSelectEnabled = false {
        didSet {
            multiSelectIndicatorView.isHidden = !multiSelectEnabled
            multiSelectIndicatorView.backgroundColor = UIColor.clear
        }
    }
    internal var document: Document? {
        didSet {
            if let data = document?.imageData {
                thumbnailImageView.image = UIImage(data: data)
            }
        }
    }
    
    override func awakeFromNib() {
        
        super.awakeFromNib()
        
        commonInit()
    }
    
    override func prepareForReuse() {
        
        super.prepareForReuse()
        
        multiSelectEnabled = false
        multiSelectIndicatorView.isHidden = true
        multiSelectIndicatorView.backgroundColor = UIColor.clear
    }
    
    override func layoutSubviews() {
        
        if onDeleteImageTouched == nil {
            deleteButtonWidthConstraint.isActive = false
            deleteButtonWidthToZeroConstraint.isActive = true
        }
        
        deleteButton.createCircularMaskLayer()

        super.layoutSubviews()
    }

    private func commonInit() {

        backgroundColor = UIColor.white
        
        deleteButton.clipsToBounds = true
        deleteButton.backgroundColor = UIColor.governmentDarkBlue()

        multiSelectIndicatorView.isHidden = true
        multiSelectIndicatorView.backgroundColor = UIColor.clear
        
        multiSelectIndicatorView.createCircularMaskLayer()
        multiSelectIndicatorView.createCircleLayer(UIColor.white)
    }

    @IBAction dynamic private func deleteImageTouched(_ sender: UIButton) {

        guard let document = document else {
            fatalError("The document should be set at this point")
        }

        onDeleteImageTouched?(document)
    }

    internal func performSelectionAnimations() {

        multiSelectIndicatorView.backgroundColor = UIColor.white
    }
    
    internal func performDeselectionAnimations() {

        multiSelectIndicatorView.backgroundColor = UIColor.clear
    }
}

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

class ImagePreviewTableViewCell: UITableViewCell {

    @IBOutlet weak var collectionView: UICollectionView!
    
    internal var previewImages: List<Document>?
    private static let imageThumbnailCellReuseID = "ImageThumbnailCellID"
    private static let addPhotoCellReuseID = "AddPhotoCellID"

    override func awakeFromNib() {

        super.awakeFromNib()

        commonInit()
    }

    override func setSelected(_ selected: Bool, animated: Bool) {

        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    private func commonInit() {
        
        let imgcell = UINib(nibName: "PreviewImageCollectionViewCell" , bundle: nil)
        collectionView.register(imgcell, forCellWithReuseIdentifier: ImagePreviewTableViewCell.imageThumbnailCellReuseID)

        collectionView.dataSource = self
    }

    private func configureCell(cell: UICollectionViewCell, atIndexPath indexPath: IndexPath) {
        
        guard let identifier = cell.reuseIdentifier, let previewImages = previewImages else {
            fatalError("Unable configure collection view cell")
        }
        
        let image = previewImages[indexPath.row]
        
        switch identifier {
        case ImagePreviewTableViewCell.imageThumbnailCellReuseID:
            (cell as! PreviewImageCollectionViewCell).document = image
            (cell as! PreviewImageCollectionViewCell).onDeleteImageTouched = { (document: Document) in
                print("I should delete \(document.id)")
            }
        default:
            ()
        }

    }
}

extension ImagePreviewTableViewCell: UICollectionViewDataSource {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {

        return previewImages?.count ?? 0
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: ImagePreviewTableViewCell.imageThumbnailCellReuseID, for: indexPath)
        
        configureCell(cell: cell, atIndexPath: indexPath)
        
        return cell
    }
}

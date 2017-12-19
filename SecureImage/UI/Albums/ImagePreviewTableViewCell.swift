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

typealias AddNewImageCallback = () -> Void
typealias ViewImageCallback = (_ document: Document) -> Void

class ImagePreviewTableViewCell: UITableViewCell {


    @IBOutlet weak var collectionView: UICollectionView!
    
    internal var previewImages: List<Document>?
    private static let imageThumbnailCellReuseID = "ImageThumbnailCellID"
    private static let addImageCellReuseID = "AddImageCellReuseID"
    internal static let cellRowSpacing: CGFloat = 5.0
    internal static let cellColumnSpacing: CGFloat = 5.0
    internal static let numberOfColumns: CGFloat = 3.0
    internal static let leftInset: CGFloat = 20.0
    internal static let rightInset: CGFloat = 15.0
    internal static let bottomInset: CGFloat = 15.0
    internal var onAddImageTouched: AddNewImageCallback?
    internal var onViewImageTouched: ViewImageCallback?
    
    internal class func collectionViewRowHeightFor(_ width: CGFloat) -> CGFloat {

        let h = ((width - (ImagePreviewTableViewCell.leftInset + ImagePreviewTableViewCell.rightInset)) - ((ImagePreviewTableViewCell.numberOfColumns - 1) *
            ImagePreviewTableViewCell.cellColumnSpacing)) / ImagePreviewTableViewCell.numberOfColumns
        return h
    }
    override func awakeFromNib() {

        super.awakeFromNib()

        commonInit()
    }

    override func setSelected(_ selected: Bool, animated: Bool) {

        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    private func commonInit() {

        let funccell = UINib(nibName: "AddImageCollectionViewCell" , bundle: nil)
        collectionView.register(funccell, forCellWithReuseIdentifier: ImagePreviewTableViewCell.addImageCellReuseID)
        
        let imgcell = UINib(nibName: "PreviewImageCollectionViewCell" , bundle: nil)
        collectionView.register(imgcell, forCellWithReuseIdentifier: ImagePreviewTableViewCell.imageThumbnailCellReuseID)

        let layout = collectionView.collectionViewLayout as! UICollectionViewFlowLayout
        layout.sectionInset = UIEdgeInsets(top: 0.0, left: ImagePreviewTableViewCell.leftInset,
                                           bottom: ImagePreviewTableViewCell.bottomInset, right: ImagePreviewTableViewCell.rightInset)

        collectionView.dataSource = self
        collectionView.delegate = self
    }

    private func configureCell(cell: UICollectionViewCell, atIndexPath indexPath: IndexPath) {
        
        guard let previewImages = previewImages else {
            fatalError("Unable configure collection view cell")
        }
        
        switch indexPath.row {
        case 0:
            ()
        default:
            let document = previewImages[indexPath.row - 1]
            (cell as! PreviewImageCollectionViewCell).document = document
            (cell as! PreviewImageCollectionViewCell).onDeleteImageTouched = { (document: Document) in
                print("I should delete \(document.id)")
            }

        }

    }
}

extension ImagePreviewTableViewCell: UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        sizeForItemAt indexPath: IndexPath) -> CGSize {

        let width = ImagePreviewTableViewCell.collectionViewRowHeightFor(collectionView.frame.size.width)

        return CGSize(width: width, height: width)
    }

    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        
        return ImagePreviewTableViewCell.cellRowSpacing
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        
        return ImagePreviewTableViewCell.cellColumnSpacing
    }
}

extension ImagePreviewTableViewCell: UICollectionViewDataSource {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {

        let count = previewImages?.count ?? 0
        return count > 5 ? 6 : count + 1 // first 5images and one additional functional cell
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        
        var cell: UICollectionViewCell
        
        switch indexPath.row {
        case 0:
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: ImagePreviewTableViewCell.addImageCellReuseID, for: indexPath)
        default:
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: ImagePreviewTableViewCell.imageThumbnailCellReuseID, for: indexPath)
        }
        
        configureCell(cell: cell, atIndexPath: indexPath)
        
        return cell
    }
}

extension ImagePreviewTableViewCell: UICollectionViewDelegate {
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        switch indexPath.row {
        case 0:
            onAddImageTouched?()
        default:
            guard let previewImages = previewImages else {
                fatalError("Unable configure collection view cell")
            }
            
            let document = previewImages[indexPath.row - 1]
            onViewImageTouched?(document)
        }
    }
}

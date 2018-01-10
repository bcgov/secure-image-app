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

    internal var previewImages: List<Document>? {
        didSet {
            self.albumCollectionViewManager.data = previewImages
            self.collectionView.reloadData()
        }
    }
    internal static let insets: UIEdgeInsets = UIEdgeInsets(top: 0.0, left: 20.0, bottom: 15.0, right: 15.0)
    internal var albumCollectionViewManager: AlbumCollectionViewManager!
    internal var onAddImageTouched: AddNewImageCallback?
    internal var onViewImageTouched: ViewImageCallback?

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
        collectionView.register(funccell, forCellWithReuseIdentifier: AlbumCollectionViewManager.addImageCellReuseID)
        
        let imgcell = UINib(nibName: "PreviewImageCollectionViewCell" , bundle: nil)
        collectionView.register(imgcell, forCellWithReuseIdentifier: AlbumCollectionViewManager.imageThumbnailCellReuseID)
        
        let layout = collectionView.collectionViewLayout as! UICollectionViewFlowLayout
        layout.sectionInset = ImagePreviewTableViewCell.insets
        
        albumCollectionViewManager = AlbumCollectionViewManager(insets: ImagePreviewTableViewCell.insets,
                                                                rowSpacing: 5.0,
                                                                columnSpacing: 5.0,
                                                                numberOfColumns: 3,
                                                                numberOfRows: 2,
                                                                data: previewImages,
                                                                delegate: self)

        collectionView.dataSource = albumCollectionViewManager
        collectionView.delegate = albumCollectionViewManager
        collectionView.allowsMultipleSelection = false
        collectionView.backgroundColor = UIColor.white
        collectionView.alpha = 0.0
        
        return albumCollectionViewManager.collectionViewRowHeightFor(width)
    }
    
    private func delete(document: Document) {

        guard let previewImages = previewImages, let index = previewImages.index(of: document) else {
            fatalError("Unable unwrap data source")
        }
        
        deleteFromDataSource(document: document)
        deleteFromCollectionView(index: index)
    }

    private func deleteFromDataSource(document: Document) {

        do {
            let realm = try Realm()
            
            if let item = realm.objects(Document.self).filter("id ==  %@", document.id).first {
                try realm.write {
                    realm.delete(item)
                }
            }
        } catch {
            print("Unable to delete object from realm")
        }
    }
    
    private func deleteFromCollectionView(index: Int) {
        
        collectionView.performBatchUpdates({
            let indexPath = IndexPath(row: index + AlbumCollectionViewManager.imageCellOffset, section: 0)
            collectionView.deleteItems(at: [indexPath])
            // If we have more items that are not displayed then we need to
            // push one onto the collection view from the back.
            if let count = previewImages?.count, count >= 5 {
                let indexPath = IndexPath(row: 5, section: 0)
                collectionView.insertItems(at: [indexPath])
            }
        }, completion: { (success) in
            self.showAddPhotosImageViewIfNeeded()
        })
    }
    
    private func showAddPhotosImageViewIfNeeded() {
        
        guard let data = albumCollectionViewManager.data, data.count == 0 else {
            return
        }
        
        let animationDuration: TimeInterval = 0.2
        
        UIView.animate(withDuration: animationDuration) {
            self.addPhotosView.alpha = 1.0
            self.collectionView.alpha = 0.0
        }
    }
    
    private func hideAddPhotosImageViewIfNeeded() {
        
        guard let data = albumCollectionViewManager.data, data.count != 0 else {
            return
        }
      
        addPhotosView.alpha = 0.0
        collectionView.alpha = 1.0
    }
    
    @objc dynamic private func addPhotoImageViewTouched(sender: UITapGestureRecognizer) {
        
        onAddImageTouched?()
    }
}

// MARK: AlbumCollectionManagerProtocol
extension ImagePreviewTableViewCell: AlbumCollectionManagerProtocol {
    
    func configureCell(cell: UICollectionViewCell, atIndexPath indexPath: IndexPath) {
        
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
                self.delete(document: document)
            }
        }
    }
    
    func viewPhoto(document: Document) {
        onViewImageTouched?(document)
    }
    
    func addPhoto() {
        onAddImageTouched?()
    }
}

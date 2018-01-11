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
// Created by Jason Leach on 2017-12-21.
//

import UIKit
import RealmSwift

class PhotosViewController: UIViewController {
    
    @IBOutlet weak var collectionView: UICollectionView!
    @IBOutlet weak var deleteButton: UIButton!
    @IBOutlet weak var itemCountLabel: UILabel!
    
    private static let animationDuration = 0.2
    private static let showImageSegueID = "ShowImageSegue"
    private static let captureImageSegueID = "CaptureImageSegue"
    private static let insets = UIEdgeInsets(top: 5.0, left: 15.0, bottom: 0.0, right: 15.0)
    private var selectedDocument: Document?
    private var albumCollectionViewManager: AlbumCollectionViewManager!
    private var selectEnabled = false
    private var selectedItems = [IndexPath]()
    internal var album: Album?
    override var prefersStatusBarHidden: Bool {
        return true
    }
    
    override func viewDidLoad() {

        super.viewDidLoad()
        
        commonInit()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        
        super.viewWillAppear(animated)
        
        updateCount()
        
        collectionView.reloadData()
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        
        if let document = selectedDocument, let dvc = segue.destination as? PhotoViewController,
            segue.identifier == PhotosViewController.showImageSegueID {
            
            dvc.document = document
            return
        }
        
        if let dvc = segue.destination as? SecureCameraViewController, segue.identifier == PhotosViewController.captureImageSegueID {
            dvc.delegate = self

            return
        }
    }
    
    override func willRotate(to toInterfaceOrientation: UIInterfaceOrientation, duration: TimeInterval) {

        collectionView.collectionViewLayout.invalidateLayout()
    }
    
    private func commonInit() {
        
        let funccell = UINib(nibName: "AddImageCollectionViewCell" , bundle: nil)
        collectionView.register(funccell, forCellWithReuseIdentifier: AlbumCollectionViewManager.addImageCellReuseID)
        
        let imgcell = UINib(nibName: "PreviewImageCollectionViewCell" , bundle: nil)
        collectionView.register(imgcell, forCellWithReuseIdentifier: AlbumCollectionViewManager.imageThumbnailCellReuseID)
        
        let layout = collectionView.collectionViewLayout as! UICollectionViewFlowLayout
        layout.sectionInset = PhotosViewController.insets
        
        albumCollectionViewManager = AlbumCollectionViewManager(insets: ImagePreviewTableViewCell.insets,
                                                                rowSpacing: 5.0,
                                                                columnSpacing: 5.0,
                                                                numberOfColumns: 3,
                                                                data: album?.documents,
                                                                delegate: self)
        
        collectionView.dataSource = albumCollectionViewManager
        collectionView.delegate = albumCollectionViewManager
        collectionView.allowsMultipleSelection = false
        collectionView.backgroundColor = UIColor.white

        deleteButton.alpha = 0.0
        deleteButton.layer.cornerRadius = deleteButton.bounds.height / 2
        deleteButton.backgroundColor = UIColor.governmentDeepYellow()
        deleteButton.setTitleColor(UIColor.blueText(), for: .normal)

        let select = UIBarButtonItem(title: "Select", style: .done, target: self, action: #selector(PhotosViewController.selectTouched(sender:)))
        navigationItem.rightBarButtonItem = select
    }
    
    @IBAction func deleteButtonTouched(sender: UIButton) {
        
        let title = "Delete Photos"
        let message = "Are you sure you want to delete the selected photos?"
        let ac = UIAlertController(title: title, message: message, preferredStyle: .alert)
        let cancel = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)
        let delete = UIAlertAction(title: "Delete", style: .default) { (sender: UIAlertAction) in
            UIView.animate(withDuration: PhotosViewController.animationDuration) {
                self.deleteButton.alpha = 0.0
            }
            
            self.deleteFromDataSource()
            self.deleteFromCollectionView()
            self.doneTouched(sender: nil)
        }
        
        ac.addAction(cancel)
        ac.addAction(delete)
        
        present(ac, animated: true, completion: nil)
    }
    
    @objc dynamic private func selectTouched(sender: UIBarButtonItem) {
       
        selectEnabled = !selectEnabled
        
        sender.title = "Done"
        sender.action = #selector(PhotosViewController.doneTouched(sender:))
        
        collectionView.allowsMultipleSelection = true
   
        for case let cell as PreviewImageCollectionViewCell in collectionView.visibleCells {
            cell.multiSelectEnabled = true
        }
        
        updateCount()
    }
    
    @objc dynamic private func doneTouched(sender: UIBarButtonItem?) {
        
        selectEnabled = !selectEnabled

        if let mySender = navigationItem.rightBarButtonItem {
            mySender.title = "Select"
            mySender.action = #selector(PhotosViewController.selectTouched(sender:))
        }
        
        selectedItems.removeAll()
        
        collectionView.allowsMultipleSelection = false
        
        for cell in collectionView.visibleCells {
            if let myCell = cell as? PreviewImageCollectionViewCell {
                myCell.multiSelectEnabled = false
            }
        }
        
        UIView.animate(withDuration: PhotosViewController.animationDuration) {
            self.deleteButton.alpha = 0.0
        }
        
        updateCount()
    }
    
    private func deleteFromDataSource() {
        
        let itemIDs = selectedItems.reduce([String]()) { (results: [String], indexPath: IndexPath) in
            guard let documents = album?.documents else {
                fatalError("Unable to unpack documents from album")
            }
            
            return results + [documents[indexPath.row - AlbumCollectionViewManager.imageCellOffset].id]
        }
        
        do {
            let realm = try Realm()
            
            let items = realm.objects(Document.self).filter("id IN %@", itemIDs)
            try realm.write {
                realm.delete(items)
            }
        } catch {
            print("Unable to delete object from realm")
        }
        
    }
    
    private func deleteFromCollectionView() {

        collectionView.performBatchUpdates({
            collectionView.deleteItems(at: selectedItems)
        }, completion: nil)
    }
    
    private func updateCount() {
        
        if let documents = album?.documents {
            if selectEnabled {
                itemCountLabel.text = "\(selectedItems.count) selected"
            } else {
                itemCountLabel.text = "\(documents.count) total"
            }
        }
    }
}

// MARK: AlbumCollectionManagerProtocol
extension PhotosViewController: AlbumCollectionManagerProtocol {

    func configureCell(cell: UICollectionViewCell, atIndexPath indexPath: IndexPath) {
        
        guard let documents = album?.documents else {
            fatalError("Unable to unpack documents from album")
        }
        
        switch indexPath.row {
        case 0:     // This will be the add image / function cell
            let myCell = (cell as! AddImageCollectionViewCell)
            myCell.shouldOffsetImage = false
        default:    // This will be everything else
            let myCell = (cell as! PreviewImageCollectionViewCell)
            myCell.document = documents[indexPath.row - AlbumCollectionViewManager.imageCellOffset]
            myCell.multiSelectEnabled = selectEnabled
        }
    }
    
    func viewPhoto(document: Document) {

        selectedDocument = document
        performSegue(withIdentifier: PhotosViewController.showImageSegueID, sender: nil)
    }
    
    func addPhoto() {

        performSegue(withIdentifier: PhotosViewController.captureImageSegueID, sender: nil)
    }
    
    func selectedCell(atIndexPath indexPath: IndexPath) {
        
        selectedItems.append(indexPath)
        updateCount()

        let cell = (collectionView.cellForItem(at: indexPath) as! PreviewImageCollectionViewCell)
        cell.performSelectionAnimations()
        
        if selectedItems.count > 0 {
            UIView.animate(withDuration: PhotosViewController.animationDuration) {
                self.deleteButton.alpha = 1.0
            }
        }
    }
    
    func deselectedCell(atIndexPath indexPath: IndexPath) {
        
        if let itemIndex = selectedItems.index(of: indexPath) {
            selectedItems.remove(at: itemIndex)
            updateCount()
        }
        
        let cell = (collectionView.cellForItem(at: indexPath) as! PreviewImageCollectionViewCell)
        cell.performDeselectionAnimations()
    }
}

// MARK: SecureCameraImageCaptureDelegate
extension PhotosViewController: SecureCameraImageCaptureDelegate {
    
    func captured(image: Data) {

        guard let album = album else {
            fatalError("Unable unwrap album")
        }

        DataServices.add(image: image, to: album)
    }
}

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

import Foundation
import UIKit
import RealmSwift

protocol AlbumCollectionManagerProtocol: class {

    // Required
    func configureCell(cell: UICollectionViewCell, atIndexPath indexPath: IndexPath)
    func viewPhoto(document: Document)
    func addPhoto()
    // Optional
    func selectedCell(atIndexPath indexPath: IndexPath)
    func deselectedCell(atIndexPath indexPath: IndexPath)
//    func configureHeader(view: UIView)
}

// Allow for default implementation of optional protocol
// functions to keep LLVM happy
extension AlbumCollectionManagerProtocol {
    
    func selectedCell(atIndexPath indexPath: IndexPath) {
        ()
    }
    
    func deselectedCell(atIndexPath indexPath: IndexPath) {
        ()
    }
    
//    func configureHeader(view: UIView) {
//        ()
//    }
}

class AlbumCollectionViewManager: NSObject {
    
    private static let collectionHeaderReuseID = "CollectionHeaderReuseID"
    internal static let imageThumbnailCellReuseID = "ImageThumbnailCellID"
    internal static let addImageCellReuseID = "AddImageCellReuseID"
    internal static let headerLabelTag = 100
    internal var data: List<Document>?
    weak internal var delegate: AlbumCollectionManagerProtocol?
    private var rowSpacing: CGFloat
    private var columnSpacing: CGFloat
    private var numberOfColumns: Int
    private var numberOfRows: Int
    private var insets: UIEdgeInsets
    
    // index represents the location in the data source. The collection view will have
    // and additional cell at index 0 so we must account for this with an offset.
    internal static let imageCellOffset = 1
    
    // When `numberOfRows` is set to zero the number of rows to show all data
    // will be used.
    init(insets: UIEdgeInsets, rowSpacing: CGFloat, columnSpacing: CGFloat, numberOfColumns: Int, numberOfRows: Int = 0, data: List<Document>? = nil, delegate: AlbumCollectionManagerProtocol? = nil) {
        
        self.insets = insets
        self.numberOfColumns = numberOfColumns
        self.numberOfRows = numberOfRows
        self.rowSpacing = rowSpacing
        self.columnSpacing = columnSpacing
        self.data = data
        self.delegate = delegate

        super.init()
    }
    
    internal func collectionViewRowHeightFor(_ width: CGFloat) -> CGFloat {

        let h = ((width - (insets.left + insets.right)) - ((CGFloat(numberOfColumns) - 1) *
            columnSpacing)) / CGFloat(numberOfColumns)

        return h
    }
}

extension AlbumCollectionViewManager: UICollectionViewDelegateFlowLayout {

    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        sizeForItemAt indexPath: IndexPath) -> CGSize {

        let width = collectionViewRowHeightFor(collectionView.frame.size.width)

        return CGSize(width: width, height: width)
    }

    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {

        return rowSpacing
    }

    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {

        return columnSpacing
    }
}

extension AlbumCollectionViewManager: UICollectionViewDataSource {

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {

        let maxCellCount = numberOfRows * numberOfColumns
        let count = data?.count ?? 0
        
        // The number of rows determines if we shuld show all data or not. If
        // zero is passed in, then we show all images *and* our function cell
        
        if maxCellCount == 0 {
            return count + AlbumCollectionViewManager.imageCellOffset
        }

        return count > maxCellCount ? maxCellCount : count + AlbumCollectionViewManager.imageCellOffset
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {

        var cell: UICollectionViewCell

        switch indexPath.row {
        case 0:
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: AlbumCollectionViewManager.addImageCellReuseID, for: indexPath)
        default:
            cell = collectionView.dequeueReusableCell(withReuseIdentifier: AlbumCollectionViewManager.imageThumbnailCellReuseID, for: indexPath)
        }

        delegate?.configureCell(cell: cell, atIndexPath: indexPath)

        return cell
    }
}

extension AlbumCollectionViewManager: UICollectionViewDelegate {

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {

        switch indexPath.row {
        case 0:
            delegate?.addPhoto()
        default:
            guard let data = data else {
                fatalError("Unable configure collection view cell")
            }

            if collectionView.allowsMultipleSelection {
                delegate?.selectedCell(atIndexPath: indexPath)
            } else {
                let document = data[indexPath.row - AlbumCollectionViewManager.imageCellOffset]
                delegate?.viewPhoto(document: document)
            }
        }
    }
    
    func collectionView(_ collectionView: UICollectionView, didDeselectItemAt indexPath: IndexPath) {

        delegate?.deselectedCell(atIndexPath: indexPath)
    }
}


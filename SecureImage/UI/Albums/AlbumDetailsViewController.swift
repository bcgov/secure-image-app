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
// Created by Jason Leach on 2017-12-14.
//

import UIKit
import RealmSwift

class AlbumDetailsViewController: UIViewController {

    @IBOutlet weak var tableView: UITableView!
    
    private static let captureImageSegueID = "CaptureImageSegue"
    private static let showImageSegueID = "ShowImageSegue"
    private static let previewCellReuseID = "ImagePreviewCellID"
    private static let functionsCellReuseID = "FunctionsCellID"
    private static let annotationCellReuseID = "AnnotationCellID"
    private static let functionsCellRowHeight: CGFloat = 60.0
    private static let annotationCellRowHeight: CGFloat = 100.0
    private static let numberOfRows: Int = 3
    internal var album: Album!
    
    override func viewDidLoad() {

        super.viewDidLoad()

        guard let _ = album else {
            fatalError("The album was not passed to \(AlbumDetailsViewController.self())")
        }
        
        commonInit()
    }

    // MARK: - Navigation

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
//        let dvc = segue.destination
//        let segueID = segue.identifier
        
        // if AlbumDetailsViewController.showImageSegueID then pass document along
        
        // if AlbumDetailsViewController.captureImageSegueID then we shoudl register as a deliage for this
        // VC so we can get the captured image back. Implement protocol.
    }

    // MARK: -

    private func commonInit() {

        tableView.dataSource = self
        tableView.delegate = self
    }
    
    private func configureCell(cell: UITableViewCell, at indexPath: IndexPath) {

        guard let identifier = cell.reuseIdentifier else {
            fatalError("Unable to determine cell reuse ID")
        }

        switch identifier {
        case AlbumDetailsViewController.previewCellReuseID:
            let cell = cell as! ImagePreviewTableViewCell
            cell.previewImages = album.documents
            cell.onViewImageTouched = { (document: Document) in
                self.performSegue(withIdentifier: AlbumDetailsViewController.showImageSegueID, sender: nil)
            }
            cell.onAddImageTouched = {
                self.performSegue(withIdentifier: AlbumDetailsViewController.captureImageSegueID, sender: nil)
            }
        default:
            ()
        }
    }
    
    private func cellIdentifierForCell(at indexPath: IndexPath) -> String {
        
        var identifier = ""

        switch indexPath.row {
        case 0:
            identifier = AlbumDetailsViewController.previewCellReuseID
        case 1:
            identifier = AlbumDetailsViewController.functionsCellReuseID
        default:
            identifier = AlbumDetailsViewController.annotationCellReuseID
        }
        
        return identifier
    }
}

// MARK: UITableViewDataSource
extension AlbumDetailsViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        return AlbumDetailsViewController.numberOfRows
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {

        let identifier = cellIdentifierForCell(at: indexPath)
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: identifier) else {
            fatalError("Unable to dequeue cell with identifier \(identifier)")
        }

        configureCell(cell: cell, at: indexPath)

        return cell
    }
}

// MARK: UITableViewDelegate
extension AlbumDetailsViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {

        let identifier = cellIdentifierForCell(at: indexPath)
        switch identifier
        {
        case AlbumDetailsViewController.previewCellReuseID:
            return ImagePreviewTableViewCell.collectionViewRowHeightFor(tableView.frame.width) * 2.0 + ImagePreviewTableViewCell.bottomInset
        case AlbumDetailsViewController.functionsCellReuseID:
            return AlbumDetailsViewController.functionsCellRowHeight
        default:
            return AlbumDetailsViewController.annotationCellRowHeight
        }
    }
    
//    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
//
//    }
}

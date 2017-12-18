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
import RealmSwift
import QuartzCore

class AlbumsViewController: UIViewController {

    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var createAlbumButton: UIButton!
    
    private static let cellAspectScaler: CGFloat = 0.533
    private static let albumCellReuseID = "AlbumCell"
    private static let albumDetailsSegueID = "ShowAlbumDetailsSegue"
    private static let albumImageViewTag = 100
    private static let albumTitleLabelTag = 101
    private var selectedAlbum: Album?
    private var albums: Results<Album>?
    private var localAlbumId: String?
    private let createFirstAlbumView: CreateFirstAlbumView = {
        let v = Bundle.main.loadNibNamed("CreateFirstAlbumView", owner: self, options: nil)?.first as! CreateFirstAlbumView
        v.translatesAutoresizingMaskIntoConstraints = false
        
        return v
    }()
    
    override func viewDidLoad() {

        super.viewDidLoad()
        
        commonInit()
    }
    
    override func viewWillAppear(_ animated: Bool) {
    
        super.viewWillAppear(animated)
        
        do {
            albums = try Realm().objects(Album.self)
            tableView.reloadData()
        } catch {
            print("Unable to load Albums")
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {

         let dvc = segue.destination as! AlbumDetailsViewController
         dvc.album  = selectedAlbum
    }
    
    private func commonInit() {

        view.addSubview(createFirstAlbumView)
        
        tableView.dataSource = self
        tableView.delegate = self

        createAlbumButton.layer.cornerRadius = createAlbumButton.frame.height / 2
        createAlbumButton.clipsToBounds = true
        
        createFirstAlbumView.widthAnchor.constraint(equalToConstant: 250).isActive = true
        createFirstAlbumView.heightAnchor.constraint(equalToConstant: 100).isActive = true
        createFirstAlbumView.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        createFirstAlbumView.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
        
        createFirstAlbumView.onCreateFirstAlbumTouched = { [weak self] in
            self?.createAlbumTouched(sender: nil)
        }
    }
    
    private func configureFor(albumsExist: Bool) {
        
        if albumsExist {
            tableView.isHidden = false
            createFirstAlbumView.isHidden = true
            createAlbumButton.isHidden = false
            
            return
        }
        
        tableView.isHidden = true
        createFirstAlbumView.isHidden = false
        createAlbumButton.isHidden = true
    }
    
    private func configureCell(cell: UITableViewCell, at indexPath: IndexPath) {
        
        guard let albums = albums, let document = albums[indexPath.row].documents.first, let imageData = document.imageData else {
            print("Unable to unpack the album")
            
            return
        }
        
//        print(albums[indexPath.row].id)
//        print(albums[indexPath.row].createdAt)
        
        if let iv = cell.viewWithTag(AlbumsViewController.albumImageViewTag) as? UIImageView {
            iv.image = UIImage(data: imageData)
        }
    }
    
    @IBAction dynamic private func createAlbumTouched(sender: Any?) {

        do {
            let album = Album()
            let realm = try Realm()

            try realm.write {
                realm.add(album)
                selectedAlbum = album
            }
        } catch {
            print("Unable to create new album in Realm")
        }
        
        performSegue(withIdentifier: AlbumsViewController.albumDetailsSegueID, sender: nil)
    }
}

// MARK: UITableViewDataSource
extension AlbumsViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        let count = albums?.count ?? 0

        configureFor(albumsExist: count == 0 ? false : true)
        return count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {

        guard let cell = tableView.dequeueReusableCell(withIdentifier: AlbumsViewController.albumCellReuseID) else {
            fatalError("Unable to dequeue cell with identifier \(AlbumsViewController.albumCellReuseID)")
        }
        
        configureCell(cell: cell, at: indexPath)

        return cell
    }
}

// MARK: UITableViewDelegate
extension AlbumsViewController: UITableViewDelegate {

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {

        return tableView.frame.size.width * AlbumsViewController.cellAspectScaler
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        guard let albums = albums else  {
            fatalError("Unable to extract album for selected row")
        }
        
        selectedAlbum = albums[indexPath.row]
        performSegue(withIdentifier: AlbumsViewController.albumDetailsSegueID, sender: nil)
    }
}

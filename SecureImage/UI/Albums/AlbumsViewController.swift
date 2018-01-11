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

class AlbumsViewController: UIViewController {

    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var createAlbumButton: UIButton!
    
    private static let cellAspectScaler: CGFloat = 0.57
    private static let albumCellReuseID = "AlbumCell"
    private static let albumDetailsSegueID = "ShowAlbumDetailsSegue"
    private var selectedAlbum: Album?
    private var albums: Results<Album>?
    private var localAlbumId: String?
    private let createFirstAlbumView: UIView = {
        let v = Bundle.main.loadNibNamed("CreateFirstAlbumView", owner: self, options: nil)?.first as! UIView
        v.translatesAutoresizingMaskIntoConstraints = false
        v.backgroundColor = UIColor.governmentDarkBlue()

        return v
    }()
    
    override func viewDidLoad() {

        super.viewDidLoad()
        
        commonInit()
    }
    
    override func viewWillAppear(_ animated: Bool) {
    
        super.viewWillAppear(animated)
        
        setNeedsStatusBarAppearanceUpdate()

        do {
            albums = try Realm().objects(Album.self).sorted(byKeyPath: "createdAt", ascending: false)
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
        createAlbumButton.backgroundColor = UIColor.governmentDeepYellow()
        createAlbumButton.setTitleColor(UIColor.blueText(), for: .normal)
        
        createFirstAlbumView.widthAnchor.constraint(equalToConstant: 300).isActive = true
        createFirstAlbumView.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        createFirstAlbumView.topAnchor.constraint(equalTo: view.topAnchor, constant: 30.0).isActive = true
        createFirstAlbumView.bottomAnchor.constraint(equalTo: createAlbumButton.topAnchor).isActive = true
    }
    
    private func configureFor(albumsExist: Bool) {
        
        if albumsExist {
            tableView.isHidden = false
            createFirstAlbumView.isHidden = true
            createAlbumButton.isHidden = false
            view.backgroundColor = UIColor.white
            
            return
        }
        
        tableView.isHidden = true
        createFirstAlbumView.isHidden = false
        createAlbumButton.isHidden = false
        view.backgroundColor = UIColor.governmentDarkBlue()
    }
    
    private func configureCell(cell: AlbumTableViewCell, at indexPath: IndexPath) {
        
        guard let albums = albums else {
            print("Unable to unpack album information")
            return
        }

        let album = albums[indexPath.row]

        if let document = album.documents.first, let imageData = document.imageData {
            cell.coverImageView.image = UIImage(data: imageData)
        }

        cell.albumTitleLabel.text = album.albumName
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

        configureFor(albumsExist: count != 0)
        
        return count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {

        guard let cell = tableView.dequeueReusableCell(withIdentifier: AlbumsViewController.albumCellReuseID) as? AlbumTableViewCell else {
            fatalError("Unable to dequeue cell with identifier \(AlbumsViewController.albumCellReuseID)")
        }
        
        configureCell(cell: cell, at: indexPath)

        return cell
    }
}

// MARK: UITableViewDelegate
extension AlbumsViewController: UITableViewDelegate {

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {

        return tableView.frame.size.width * AlbumsViewController.cellAspectScaler + AlbumTableViewCell.fauxCellSpaceOffset
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        guard let albums = albums else  {
            fatalError("Unable to extract album for selected row")
        }
        
        selectedAlbum = albums[indexPath.row]
        performSegue(withIdentifier: AlbumsViewController.albumDetailsSegueID, sender: nil)
    }
}

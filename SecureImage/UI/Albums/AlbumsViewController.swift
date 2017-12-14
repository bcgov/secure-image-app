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

class AlbumsViewController: UIViewController {

    @IBOutlet weak var tableView: UITableView!
    
    private let albumDetailsSequeID: String = "ShowAlbumDetailsSegue"
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

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
         let dvc = segue.destination as! AlbumDetailsViewController
         dvc.localAlbumID  = "11111111111"
    }
    
    private func commonInit() {
        tableView.isHidden = true
        
        view.addSubview(createFirstAlbumView)
        
        createFirstAlbumView.widthAnchor.constraint(equalToConstant: 250).isActive = true
        createFirstAlbumView.heightAnchor.constraint(equalToConstant: 100).isActive = true
        createFirstAlbumView.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        createFirstAlbumView.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
        
        createFirstAlbumView.onCreateFirstAlbumTouched = { [weak self] in
            // Create a new album in the db
            // Pass album ID to Album Details VC

            guard let sequeID = self?.albumDetailsSequeID else {
                return
            }

            self?.performSegue(withIdentifier: sequeID, sender: nil)
        }
    }
}

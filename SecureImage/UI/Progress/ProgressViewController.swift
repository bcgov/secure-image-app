//
// SecureImage
//
// Copyright © 2018 Province of British Columbia
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
// Created by Jason Leach on 2018-01-17.
//

import UIKit

class ProgressViewController: UIViewController {

    private let progressView: ProgressView = {
        let v = Bundle.main.loadNibNamed("ProgressView", owner: self, options: nil)?.first as! ProgressView
        v.translatesAutoresizingMaskIntoConstraints = false
        v.backgroundColor = Theme.governmentDarkBlue
        v.layer.cornerRadius = 12.0

        return v
    }()
    
    override func viewDidLoad() {
        
        super.viewDidLoad()

        view.addSubview(progressView)

        progressView.widthAnchor.constraint(equalToConstant: 300).isActive = true
        progressView.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
        progressView.heightAnchor.constraint(equalToConstant: 300).isActive = true
        progressView.centerYAnchor.constraint(equalTo: view.centerYAnchor).isActive = true
    }

    internal func updateProgress(to: Float) {
        
        progressView.animateProgressViewToProgress(to)
    }
}

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
// Created by Jason Leach on 2017-12-20.
//

import UIKit

typealias AlbumPropertyChangedCallback = (_ property: String, _ value: String) -> Void

class AlbumPropertyTableViewCell: UITableViewCell {

    @IBOutlet weak var attributeTitleLabel: UILabel!
    @IBOutlet weak var attributeTextField: UITextField!
    
    internal var onPropertyChanged: AlbumPropertyChangedCallback?
    
    override func awakeFromNib() {
        
        super.awakeFromNib()
        
        attributeTextField.delegate = self
    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {

        if let touch = event?.allTouches?.first, let aView = touch.view {
            if attributeTextField.isFirstResponder && attributeTextField != aView {
                attributeTextField.resignFirstResponder()
            }
        }
        
        super.touchesBegan(touches, with: event)
    }
}

// MARK: UITextFieldDelegate
extension AlbumPropertyTableViewCell: UITextFieldDelegate {
    
    //    func textFieldDidBeginEditing(_ textField: UITextField) {
    //        print("BEGIN EDIT")
    //    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        
        textField.resignFirstResponder()
        
        return false
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        
        guard let key = attributeTitleLabel.text?.toCammelCase(), let value = attributeTextField.text else {
            return
        }
        
        onPropertyChanged?(key, value)
    }
}

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
//import FeedHenry

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // FH.init using Swift FH sdk
    // trailing closure Swift syntax
//    FH.init { (resp:Response, error: NSError?) -> Void in
//        if let error = error {
//            self.statusLabel.text = "FH init in error \(error.localizedDescription)"
//            print("Error: \(error)")
//            return
//        }
//        self.statusLabel.text = "FH init successful"
//        print("Response: \(resp.parsedResponse)")
//    }
}

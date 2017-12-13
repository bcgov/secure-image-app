//
//  ViewController.swift
//  SecureImage
//
//  Created by Jason Leach on 2017-12-13.
//  Copyright © 2017 Pathfinder. All rights reserved.
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

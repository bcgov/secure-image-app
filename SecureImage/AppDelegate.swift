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

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    let lockScreenWindow = LockScreenWindow(frame: UIScreen.main.bounds)
    let dataServices: DataServices?

    override init() {
        // Any Realm management must be done before accessing `Realm()` for the first time
        // otherwise realm will initalize with the default configuraiton.
        // Realm must be initalized here, in `init` because `didFinishLaunchingWithOptions`
        // often executes after `viewDidLoad` et al.
        dataServices = DataServices.shared
        super.init()
    }
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
 
        #if (arch(i386) || arch(x86_64)) && os(iOS) && DEBUG
            // so we can find our Documents
            print("documents = \(DataServices.documentsURL())")
        #endif
        
        NotificationCenter.default.addObserver(forName: Notification.Name.userAuthenticated, object: nil, queue:nil) { [weak self] _ in
            self?.hideLockScreen()
        }

        lockScreenWindow.show()
        
        return true
    }

    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }
    
     // MARK:
    
    func hideLockScreen(animated: Bool = true) {
        lockScreenWindow.hide(animated: animated, completion: { [weak self] in
            self?.window?.makeKeyAndVisible()
        })
    }
}

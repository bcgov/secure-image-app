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
// Created by Jason Leach on 2018-01-16.
//

import Foundation

typealias CompletionCallback = (_ completionHandler: @escaping (() -> Void)) -> Void

class AsyncOperation: ConcurrentOperation {
    
    private let callback: CompletionCallback
    
    class func asyncOperation(callback: @escaping CompletionCallback) -> AsyncOperation {

        return AsyncOperation(callback: callback)
    }
    
    init(callback: @escaping CompletionCallback) {

        self.callback = callback
        super.init()
    }
    
    override func start() {

        state = .Executing
        
        callback {
            self.state = .Finished
        }
    }
}

extension OperationQueue {

    func addAsyncOperation(callback: @escaping CompletionCallback) {

        addOperation(AsyncOperation(callback: callback))
    }
}

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

//
//  Created by Caleb Davenport on 7/7/14.
//
//  https://gist.github.com/calebd/93fa347397cec5f88233
//

import Foundation

typealias SuccessHandler = (_ operation: Operation) -> Void
typealias FailureHandler = (_ operation: Operation, _ error: Error?) -> Void

class ConcurrentOperation: Operation {
    
    internal var success: SuccessHandler?
    internal var failure: FailureHandler?
    
    // MARK: - Types
    
    enum State {

        case Ready, Executing, Finished
        func keyPath() -> String {
            switch self {
            case .Ready:
                return "isReady"
            case .Executing:
                return "isExecuting"
            case .Finished:
                return "isFinished"
            }
        }
    }
    
    // MARK: - Properties
    
    var state = State.Ready {

        willSet {
            willChangeValue(forKey: newValue.keyPath())
            willChangeValue(forKey: state.keyPath())
        }
        didSet {
            didChangeValue(forKey: oldValue.keyPath())
            didChangeValue(forKey: state.keyPath())
        }
    }
    
    // MARK: - Operation
    
    override var isReady: Bool {

        // To ensure dependencies area satisfied we
        // need to make sure super is ready.
        return super.isReady && state == .Ready
    }
    
    override var isExecuting: Bool {

        return state == .Executing
    }
    
    override var isFinished: Bool {

        return state == .Finished
    }
    
    override var isAsynchronous: Bool {

        return true
    }
}


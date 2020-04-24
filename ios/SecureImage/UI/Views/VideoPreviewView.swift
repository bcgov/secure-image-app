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
// Created by Jason Leach on 2018-01-03.
//

import UIKit
import AVFoundation

class VideoPreviewView: UIView {
    
    internal var videoPreviewLayer: AVCaptureVideoPreviewLayer {
        return layer as! AVCaptureVideoPreviewLayer
    }
    internal var session: AVCaptureSession? {
        get { return videoPreviewLayer.session }
        set(newValue) { videoPreviewLayer.session = newValue }
    }
    private var orientationMap: [UIDeviceOrientation : AVCaptureVideoOrientation] = [
        .landscapeRight     : .landscapeLeft,
        .landscapeLeft      : .landscapeRight,
        .portrait           : .portrait,
        .portraitUpsideDown : .portraitUpsideDown
    ]
    
    override class var layerClass: AnyClass {
        
        return AVCaptureVideoPreviewLayer.self
    }
    
    required init?(coder aDecoder: NSCoder) {
        
        super.init(coder: aDecoder)
        
        // default, but lets be clear about it
        (layer as! AVCaptureVideoPreviewLayer).videoGravity = AVLayerVideoGravity.resizeAspect
    }

//    override func layoutSubviews() {
//
//        super.layoutSubviews()
//
////        layer.bounds = self.bounds
//
//
////        CGRect frame = self.cameraPreviewView.layer.frame;
////        frame.origin.y = -kYJCameraPreviewViewOffset;
////        self.cameraPreviewView.layer.frame = frame;
//    }

    internal func updateVideoOrientationForDeviceOrientation() {

        let deviceOrientation = UIDevice.current.orientation

        if let videoPreviewLayerConnection = videoPreviewLayer.connection {
            guard let newVideoOrientation = orientationMap[deviceOrientation], (deviceOrientation.isPortrait || deviceOrientation.isLandscape)
                else {
                    return
            }

            videoPreviewLayerConnection.videoOrientation = newVideoOrientation
        }
    }
}

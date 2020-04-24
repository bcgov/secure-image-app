package ca.bc.gov.secureimage.screens.securecamera

import ca.bc.gov.secureimage.common.base.BasePresenter
import ca.bc.gov.secureimage.common.base.BaseView
import com.wonderkiln.camerakit.CameraKitError
import com.wonderkiln.camerakit.CameraKitEvent
import com.wonderkiln.camerakit.CameraKitImage
import com.wonderkiln.camerakit.CameraKitVideo


/**
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Aidan Laing on 2017-12-12.
 *
 */
interface SecureCameraContract {

    interface View : BaseView<Presenter> {
        fun finish()

        fun setCapturing(capturing: Boolean)

        fun showMessage(message: String)
        fun showError(message: String)

        fun setCameraMethod(cameraMethod: Int)
        fun setCameraCropOutput(cropOutput: Boolean)
        fun setCameraPermissions(permissions: Int)
        fun setCameraFocus(focus: Int)
        fun setPinchToZoom(pinchToZoom: Boolean)

        fun startCamera()
        fun stopCamera()

        fun setUpCameraListener()

        fun setUpCaptureImageListener()
        fun showCaptureImage()
        fun hideCaptureImage()
        fun captureImage()

        fun setUpBackListener()
        fun showBack()
        fun hideBack()

        fun setCameraFlash(flash: Int)
        fun setUpFlashControlListener()
        fun showFlashControl()
        fun hideFlashControl()
        fun showFlashOff()
        fun showFlashOn()
        fun showFlashAuto()

        fun setImageCounterText(text: String)
        fun showImageCounter()
        fun hideImageCounter()

        fun showShutter()
        fun hideShutter()
    }

    interface Presenter : BasePresenter {
        fun viewShown()
        fun viewHidden()

        fun captureImageClicked(capturing: Boolean)

        fun onCameraEvent(event: CameraKitEvent?)
        fun onCameraError(error: CameraKitError?)
        fun onCameraImage(image: CameraKitImage?, createCameraImage: Boolean)
        fun onCameraVideo(video: CameraKitVideo?)

        fun backClicked()

        fun flashControlClicked(flashMode: Int)
    }

}
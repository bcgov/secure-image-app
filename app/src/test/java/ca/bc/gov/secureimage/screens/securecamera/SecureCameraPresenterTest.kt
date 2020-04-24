package ca.bc.gov.secureimage.screens.securecamera

import ca.bc.gov.secureimage.RxImmediateSchedulerRule
import ca.bc.gov.secureimage.common.managers.CompressionManager
import ca.bc.gov.secureimage.data.models.Location
import ca.bc.gov.secureimage.data.repos.albums.AlbumsRepo
import ca.bc.gov.secureimage.data.repos.cameraimages.CameraImagesRepo
import ca.bc.gov.secureimage.data.repos.locationrepo.LocationRepo
import com.github.florent37.rxgps.RxGps
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import com.wonderkiln.camerakit.CameraKit
import com.wonderkiln.camerakit.CameraKitEvent
import com.wonderkiln.camerakit.CameraKitImage
import io.reactivex.Observable
import org.junit.After
import org.junit.Test

import org.junit.Before
import org.junit.ClassRule

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
class SecureCameraPresenterTest {

    companion object {
        @ClassRule
        @JvmField
        val rxSchedulers = RxImmediateSchedulerRule()
    }

    private lateinit var view: SecureCameraContract.View

    private val albumKey = "albumKey"

    private lateinit var cameraImagesRepo: CameraImagesRepo

    private lateinit var albumsRepo: AlbumsRepo

    private lateinit var locationRepo: LocationRepo

    private lateinit var rxGps: RxGps

    private lateinit var compressionManager: CompressionManager

    private lateinit var presenter: SecureCameraPresenter

    @Before
    fun setUp() {
        view = mock()

        cameraImagesRepo = mock()
        albumsRepo = mock()
        locationRepo = mock()
        rxGps = mock()
        compressionManager = mock()

        presenter = SecureCameraPresenter(view, albumKey, cameraImagesRepo, albumsRepo,
                locationRepo, rxGps, compressionManager)
    }

    @After
    fun tearDown() {
        CameraImagesRepo.destroyInstance()
        AlbumsRepo.destroyInstance()
        LocationRepo.destroyInstance()
    }

    @Test
    fun presenterSet() {
        verify(view).presenter = presenter
    }
/*
    @Test
    fun subscribe() {
        val location = Location(48.123, -123.123)
        whenever(locationRepo.getLocation(rxGps, true)).thenReturn(Observable.just(location))

        presenter.subscribe()

        verify(view).hideShutter()

        verify(view).setCameraMethod(CameraKit.Constants.METHOD_STILL)
        verify(view).setCameraFlash(CameraKit.Constants.FLASH_OFF)
        verify(view).showFlashOff()
        verify(view).setUpFlashControlListener()

        verify(view).setCameraCropOutput(false)
        verify(view).setCameraPermissions(CameraKit.Constants.PERMISSIONS_PICTURE)
        verify(view).setCameraFocus(CameraKit.Constants.FOCUS_TAP_WITH_MARKER)
        verify(view).setPinchToZoom(true)

        verify(view).setUpCameraListener()

        verify(view).setUpCaptureImageListener()
        verify(view).setCapturing(false)

        verify(view).setUpBackListener()
    }
*/

    @Test
    fun viewShown() {
        val location = Location(48.123, -123.123)
        whenever(locationRepo.getLocation(rxGps, true)).thenReturn(Observable.just(location))

        val albumCount = 5
        whenever(cameraImagesRepo.getCameraImageCountInAlbum(albumKey)).thenReturn(Observable.just(albumCount))

        presenter.viewShown()

        verify(view).setCapturing(false)

        verify(view).hideShutter()
        verify(view).hideCaptureImage()
        verify(view).hideBack()
        verify(view).hideFlashControl()
        verify(view).hideImageCounter()

        verify(view).startCamera()
    }

    @Test
    fun viewHidden() {
        presenter.viewHidden()

        verify(view).stopCamera()
    }

    @Test
    fun getLocationAndCache() {
        val location = Location(48.123, -123.123)
        whenever(locationRepo.getLocation(rxGps, true)).thenReturn(Observable.just(location))

        presenter.getLocationAndCache()

        verify(view).presenter = presenter
        verifyNoMoreInteractions(view)
    }

    @Test
    fun captureImageClickedCapturing() {
        val capturing = true
        presenter.captureImageClicked(capturing)

        verify(view).presenter = presenter
        verifyNoMoreInteractions(view)
    }

    @Test
    fun captureImageClickedNotCapturing() {
        val capturing = false
        presenter.captureImageClicked(capturing)

        verify(view).setCapturing(true)
        verify(view).showShutter()
        verify(view).captureImage()
    }

    @Test
    fun onCameraEventOpen() {
        val cameraKitEvent: CameraKitEvent = mock()
        whenever(cameraKitEvent.type).thenReturn(CameraKitEvent.TYPE_CAMERA_OPEN)
        presenter.onCameraEvent(cameraKitEvent)

        verify(view).showCaptureImage()
        verify(view).showBack()
        verify(view).showFlashControl()
        verify(view).showImageCounter()
    }

    @Test
    fun onCameraEventClose() {
        val cameraKitEvent: CameraKitEvent = mock()
        whenever(cameraKitEvent.type).thenReturn(CameraKitEvent.TYPE_CAMERA_CLOSE)
        presenter.onCameraEvent(cameraKitEvent)

        verify(view).presenter = presenter
        verifyNoMoreInteractions(view)
    }

    @Test
    fun onCameraImage() {
        val cameraKitImage: CameraKitImage = mock()
        whenever(cameraKitImage.jpeg).thenReturn(ByteArray(64))
        presenter.onCameraImage(cameraKitImage, false)

        verify(view).hideShutter()
    }

    @Test
    fun onCameraImageNull() {
        presenter.onCameraImage(null, false)

        verify(view).hideShutter()
        verify(view).setCapturing(false)
    }

    @Test
    fun backClicked() {
        presenter.backClicked()

        verify(view).finish()
    }

    @Test
    fun flashControlClickedFlashOff() {
        val flashMode = CameraKit.Constants.FLASH_OFF
        presenter.flashControlClicked(flashMode)

        verify(view).showFlashOff()
        verify(view).setCameraMethod(CameraKit.Constants.METHOD_STILL)
    }

    @Test
    fun flashControlClickedFlashOn() {
        val flashMode = CameraKit.Constants.FLASH_ON
        presenter.flashControlClicked(flashMode)

        verify(view).showFlashOn()
        verify(view).setCameraMethod(CameraKit.Constants.METHOD_STANDARD)
    }

    @Test
    fun flashControlClickedFlashAuto() {
        val flashMode = CameraKit.Constants.FLASH_AUTO
        presenter.flashControlClicked(flashMode)

        verify(view).showFlashAuto()
        verify(view).setCameraMethod(CameraKit.Constants.METHOD_STANDARD)
    }
}
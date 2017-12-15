package ca.bc.gov.secureimage.screens.securecamera

import ca.bc.gov.secureimage.data.models.CameraImage
import ca.bc.gov.secureimage.data.repos.albums.AlbumsRepo
import com.wonderkiln.camerakit.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * Created by Aidan Laing on 2017-12-13.
 *
 */
class SecureCameraPresenter(
        private val view: SecureCameraContract.View,
        private val albumKey: String,
        private val albumsRepo: AlbumsRepo
) : SecureCameraContract.Presenter {

    private val disposables = CompositeDisposable()

    init {
        view.presenter = this
    }

    override fun subscribe() {
        view.setCameraMethod(CameraKit.Constants.METHOD_STANDARD)
        view.setCameraCropOutput(false)
        view.setCameraPermissions(CameraKit.Constants.PERMISSIONS_PICTURE)
        view.setCameraFocus(CameraKit.Constants.FOCUS_TAP_WITH_MARKER)
        view.setPinchToZoom(true)

        view.setUpCameraListener()

        view.setUpCaptureImageListener()

        view.setUpBackListener()

        view.setUpDoneListener()

        view.setCameraFlash(CameraKit.Constants.FLASH_OFF)
        view.showFlashOff()
        view.setUpFlashControlListener()
    }

    override fun dispose() {
        disposables.dispose()
    }

    override fun viewShown() {
        view.hideCaptureImage()
        view.hideBack()
        view.hideDone()
        view.hideFlashControl()
        view.hideImageCounter()

        view.startCamera()
    }

    override fun viewHidden() {
        view.stopCamera()
    }

    // Camera listener events
    override fun onCameraEvent(event: CameraKitEvent?) {
        when (event?.type) {
            CameraKitEvent.TYPE_CAMERA_OPEN -> {
                view.showCaptureImage()
                view.showBack()
                view.showDone()
                view.showFlashControl()
            }
        }
    }

    override fun onCameraError(error: CameraKitError?) {

    }

    override fun onCameraImage(image: CameraKitImage?) {
        if (image == null) return

        val cameraImage = CameraImage()
        cameraImage.byteArray = image.jpeg
        saveCameraImage(cameraImage)
    }

    /**
     * Gets current album, adds the new image to the album's images and saves
     * Updates album's updated time
     * On success shows the new image count
     */
    private fun saveCameraImage(cameraImage: CameraImage) {
        albumsRepo.getAlbum(albumKey)
                .take(1)
                .observeOn(Schedulers.io())
                .flatMap {
                    val images = it.cameraImages
                    images.add(cameraImage)

                    it.cameraImages = images
                    it.updatedTime = System.currentTimeMillis()

                    albumsRepo.saveAlbum(it)
                }
                .map { it.cameraImages.size }
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    view.showError(it.message ?: "Error saving image")
                },
                onSuccess = {
                    val imageCounterText = "$it ${if (it == 1) "Image" else "Images"}"
                    view.setImageCounterText(imageCounterText)
                    view.showImageCounter()
                }
        ).addTo(disposables)
    }

    override fun onCameraVideo(video: CameraKitVideo?) {

    }

    // Take image
    override fun takeImageClicked() {
        view.captureImage()
    }

    // Back
    override fun backClicked() {
        view.finish()
    }

    // Done
    override fun doneClicked() {
        view.finish()
    }

    // Flash control
    override fun flashControlClicked(flashMode: Int) {
        when (flashMode) {
            CameraKit.Constants.FLASH_OFF -> view.showFlashOff()
            CameraKit.Constants.FLASH_ON -> view.showFlashOn()
            CameraKit.Constants.FLASH_AUTO -> view.showFlashAuto()
        }
    }
}
package ca.bc.gov.secureimage.screens.createalbum

import ca.bc.gov.mobileauthentication.MobileAuthenticationClient
import ca.bc.gov.mobileauthentication.common.exceptions.RefreshExpiredException
import ca.bc.gov.mobileauthentication.common.exceptions.TokenNotFoundException
import ca.bc.gov.secureimage.common.managers.NetworkManager
import ca.bc.gov.secureimage.data.AppApi
import ca.bc.gov.secureimage.data.models.AddImages
import ca.bc.gov.secureimage.data.models.local.CameraImage
import ca.bc.gov.secureimage.data.repos.albums.AlbumsRepo
import ca.bc.gov.secureimage.data.repos.cameraimages.CameraImagesRepo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

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
class CreateAlbumPresenter(
        private val view: CreateAlbumContract.View,
        private val albumKey: String,
        private val albumsRepo: AlbumsRepo,
        private val cameraImagesRepo: CameraImagesRepo,
        private val networkManager: NetworkManager,
        private val appApi: AppApi,
        override val mobileAuthenticationClient: MobileAuthenticationClient
) : CreateAlbumContract.Presenter {

    private val disposables = CompositeDisposable()

    init {
        view.presenter = this
    }

    override fun subscribe() {
        view.hideNetworkType()

        view.setBacked(false)
        view.setUpBackListener()

        view.setAlbumDeleted(false)
        view.setUpDeleteAlbumListener()

        view.setRefresh(true)
        view.setUpAddImagesListener()
        view.hideImagesLoading()
        view.setUpImagesList()
        view.setUpViewAllImagesListener()

        view.setUpUploadListener()

        getAlbumFields()
    }

    override fun dispose() {
        view.hideDeleteAlbumDialog()
        view.hideDeleteImageDialog()
        view.hideDeletingDialog()
        view.hideUploadingDialog()
        view.hideMobileNetworkWarningDialog()
        view.hideNoConnectionDialog()

        disposables.dispose()
    }

    override fun viewShown(refresh: Boolean, addNetworkListener: Boolean) {
        view.setBacked(false)

        if (refresh) {
            view.setRefresh(false)

            view.hideAddImagesLayout()
            view.hideViewAllImages()
            view.hideUpload()
            getImages()
        }

        if (addNetworkListener) addNetworkTypeListener()
    }

    /**
     * Clears all disposables
     * Saves album fields if back was not clicked and album is not deleted
     */
    override fun viewHidden(backed: Boolean, albumDeleted: Boolean, albumName: String, comments: String) {
        mobileAuthenticationClient.clear()
        disposables.clear()

        if (!backed && !albumDeleted) {
            saveAlbumFields(albumName, comments, false)
        }
    }

    /**
     * Pings the network every 5 seconds to check if connected/disconnected or on wifi/mobile
     * Initial delay is 0 to instantly check on authenticate up
     */
    fun addNetworkTypeListener(
            initialDelay: Long = 0,
            period: Long = 5,
            timeUnit: TimeUnit = TimeUnit.SECONDS
    ) {
        networkManager.getNetworkTypeListener(initialDelay, period, timeUnit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    view.showError(it.message ?: "Error listening to network status")
                },
                onNext = { networkType ->
                    networkTypeChanged(networkType)
                }
        ).addTo(disposables)
    }

    fun networkTypeChanged(networkType: NetworkManager.NetworkType) {
        when (networkType) {
            NetworkManager.NetworkType.WifiConnection -> {
                view.hideNetworkType()
                view.clearNetworkTypeText()
            }
            NetworkManager.NetworkType.MobileConnection -> {
                view.showNetworkType()
                view.setNetworkTypeTextMobileConnection()
            }
            NetworkManager.NetworkType.NoConnection -> {
                view.showNetworkType()
                view.setNetworkTypeTextNoConnection()
            }
        }
    }

    /**
     * Gets album and populates album fields
     */
    fun getAlbumFields() {
        albumsRepo.getAlbum(albumKey)
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    view.showError(it.message ?: "Error retrieving album")
                },
                onSuccess = { album ->
                    view.setAlbumName(album.name)
                    view.setComments(album.comments)
                }
        ).addTo(disposables)
    }

    /**
     * Gets album images and sorts by first created
     * On success show images with add images model so recycler view can display an add image tile
     */
    fun getImages(addImages: AddImages = AddImages()) {
        cameraImagesRepo.getAllCameraImagesInAlbum(albumKey)
                .flatMapIterable { it }
                .toSortedList { cameraImage1, cameraImage2 -> cameraImage1.compareTo(cameraImage2) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    view.showImages(ArrayList())
                    view.showImagesLoading()
                }.subscribeBy(
                onError = {
                    view.hideImagesLoading()
                    view.showError(it.message ?: "Error processing images")
                },
                onSuccess = { images ->
                    view.hideImagesLoading()
                    albumSizeReturned(images.size)

                    if (images.size > 0) {
                        val items = ArrayList<Any>()
                        items.add(addImages)
                        items.addAll(images)
                        view.showImages(items)
                    }
                }
        ).addTo(disposables)
    }

    /**
     * Saves album fields
     */
    override fun backClicked(saveAlbum: Boolean, albumName: String, comments: String) {
        view.setBacked(true)
        if (saveAlbum) saveAlbumFields(albumName, comments, true)
    }

    /**
     * Add all current album fields to existing album and saves
     * Update time is set to current time in millis
     * On success finishes
     */
    fun saveAlbumFields(albumName: String, comments: String, finish: Boolean) {
        albumsRepo.getAlbum(albumKey)
                .observeOn(Schedulers.io())
                .flatMap { album ->
                    album.name = albumName
                    album.comments = comments
                    album.updatedTime = System.currentTimeMillis()
                    albumsRepo.saveAlbum(album)
                }
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    view.showError(it.message ?: "Error saving album fields")
                },
                onSuccess = {
                    if (finish) view.finish()
                })
                .addTo(disposables)
    }

    // Delete album
    override fun deleteAlbumClicked() {
        view.showDeleteAlbumDialog()
    }


    override fun deleteAlbumConfirmed() {
        deleteAlbum()
    }

    /**
     * Deletes album model then deletes all the images associated with that album
     */
    fun deleteAlbum() {
        albumsRepo.deleteAlbum(albumKey)
                .observeOn(Schedulers.io())
                .flatMap { cameraImagesRepo.deleteAllCameraImagesInAlbum(albumKey) }
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    view.showDeletingDialog()
                }.subscribeBy(
                onError = {
                    view.hideDeletingDialog()
                    view.showError(it.message ?: "Error deleting")
                },
                onSuccess = {
                    view.setAlbumDeleted(true)
                    view.hideDeletingDialog()
                    view.showAlbumDeletedMessage()
                    view.finish()
                }
        ).addTo(disposables)
    }

    // View all
    override fun viewAllImagesClicked() {
        view.setRefresh(true)
        view.goToAllImages(albumKey)
    }

    fun getViewAllText(albumSize: Int): String {
        return "View all Images ($albumSize)"
    }

    // Add images
    override fun addImagesClicked() {
        view.setRefresh(true)
        view.goToSecureCamera(albumKey)
    }

    // Image click
    override fun imageClicked(cameraImage: CameraImage, position: Int) {
        view.goToImageDetail(cameraImage.albumKey, position - 1)
    }

    // Image deletion
    override fun imageDeleteClicked(cameraImage: CameraImage, position: Int) {
        view.showDeleteImageDialog(cameraImage, position)
    }

    override fun deleteImageConfirmed(cameraImage: CameraImage, position: Int) {
        deleteImage(cameraImage, position)
    }

    /**
     * Deletes camera image from local storage
     * On success notifies image list that an item has been removed
     */
    fun deleteImage(cameraImage: CameraImage, position: Int) {
        cameraImagesRepo.deleteCameraImage(cameraImage)
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    view.showError(it.message ?: "Error deleting image")
                },
                onSuccess = { image ->
                    view.showImageDeletedMessage()
                    view.notifyImageRemoved(image, position)
                    checkAlbumImageCountForNewState()
                }
        ).addTo(disposables)
    }

    /**
     * Gets the current count of images in album to check to see
     */
    fun checkAlbumImageCountForNewState() {
        cameraImagesRepo.getCameraImageCountInAlbum(albumKey)
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    view.showError(it.message ?: "Error saving image")
                },
                onSuccess = { albumSize ->
                    albumSizeReturned(albumSize)
                }
        ).addTo(disposables)
    }

    // Updates state of view based on if the album is empty or not
    fun albumSizeReturned(albumSize: Int) {
        if (albumSize == 0) {
            view.showAddImagesLayout()
            view.hideViewAllImages()
            view.hideUpload()

        } else {
            view.hideAddImagesLayout()
            view.showViewAllImages()
            view.setViewAllImagesText(getViewAllText(albumSize))
            view.showUpload()
        }
    }

    // Upload album
    override fun uploadClicked(albumName: String, comments: String) {
        saveAlbumFieldsForUpload(albumName, comments)
    }

    /**
     * Saves the fields before upload
     */
    fun saveAlbumFieldsForUpload(albumName: String, comments: String) {
        albumsRepo.getAlbum(albumKey)
                .observeOn(Schedulers.io())
                .flatMap { album ->
                    album.name = albumName
                    album.comments = comments
                    album.updatedTime = System.currentTimeMillis()
                    albumsRepo.saveAlbum(album)
                }
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    view.showError(it.message ?: "Error saving album fields")
                },
                onSuccess = {
                    checkNetworkTypeForUpload(networkManager.getNetworkType())
                })
                .addTo(disposables)
    }

    /**
     * Determines if upload is viable, a warning needs to be showed, or not possible
     */
    fun checkNetworkTypeForUpload(networkType: NetworkManager.NetworkType) {
        when (networkType) {
            NetworkManager.NetworkType.WifiConnection -> checkAuthClientForValidToken()
            NetworkManager.NetworkType.MobileConnection -> view.showMobileNetworkWarningDialog()
            NetworkManager.NetworkType.NoConnection -> view.showNoConnectionDialog()
        }
    }

    override fun uploadAnywayClicked() {
        checkAuthClientForValidToken()
    }

    /**
     * Checks a throwable to see if authentication flow needs to be reinitiated
     */
    fun checkThrowableForAuthenticateLaunch(throwable: Throwable) {
        when (throwable) {
            is TokenNotFoundException -> mobileAuthenticationClient.authenticate()
            is RefreshExpiredException -> mobileAuthenticationClient.authenticate()
            is NoSuchElementException -> mobileAuthenticationClient.authenticate()
            else -> view.showError(throwable.message ?: "Error uploading")
        }
    }

    /**
     * Checks to make sure a valid token is stored in the mobile authentication client
     */
    fun checkAuthClientForValidToken() {
        mobileAuthenticationClient.getTokenAsObservable()
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    checkThrowableForAuthenticateLaunch(it)
                },
                onSuccess = {
                    getCameraImageCountForUploadingDialog()
                }
        ).addTo(disposables)
    }

    override fun authenticationSuccess() {
        getCameraImageCountForUploadingDialog()
    }

    /**
     * Gets the image count to populate the uploading dialog with the number of images to be
     * uploaded.
     */
    fun getCameraImageCountForUploadingDialog() {
        cameraImagesRepo.getCameraImageCountInAlbum(albumKey)
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    view.showError(it.message ?: "Error saving image")
                },
                onSuccess = { albumSize ->
                    view.showUploadingDialog(albumSize)
                    getAlbumNameForUpload()
                }
        ).addTo(disposables)
    }

    /**
     * Gets album name that will be used for
     */
    fun getAlbumNameForUpload() {
        albumsRepo.getAlbum(albumKey)
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    view.showError(it.message ?: "Error retrieving album")
                },
                onSuccess = { album ->
                    createRemoteAlbumId(album.name)
                }
        ).addTo(disposables)
    }

    /**
     * Gets a remote album id that can be used to upload images to and build a download url
     */
    fun createRemoteAlbumId(albumName: String) {
        mobileAuthenticationClient.getTokenAsObservable()
                .flatMap { token ->
                    val authToken = "${token.bearer} ${token.accessToken}"
                    appApi.createRemoteAlbumId(authToken)
                }
                .map { it.remoteAlbumId }
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    checkThrowableForAuthenticateLaunch(it)
                    view.hideUploadingDialog()
                },
                onSuccess = { remoteAlbumId ->
                    uploadImagesToRemoteAlbum(remoteAlbumId, albumName)
                }
        ).addTo(disposables)
    }

    /**
     * Uploads all the images to the specified remote album id
     * Updates uploaded count in uploading dialog after each upload has finished
     * Builds download url once all images have been uploaded
     */
    fun uploadImagesToRemoteAlbum(remoteAlbumId: String, albumName: String) {
        cameraImagesRepo.getAllCameraImagesInAlbum(albumKey)
                .flatMapIterable { it }
                .flatMap { cameraImagesRepo.uploadCameraImage(remoteAlbumId, it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    view.showError(it.message ?: "Error uploading album")
                    view.hideUploadingDialog()
                },
                onNext = {
                    view.incrementUploadedCount()
                },
                onComplete = {
                    buildDownloadUrl(remoteAlbumId, albumName)
                }
        ).addTo(disposables)
    }

    /**
     * Gets a download url which will download a zip file of all the uploaded image associated with
     * that remote album id.
     */
    fun buildDownloadUrl(remoteAlbumId: String, albumName: String) {
        mobileAuthenticationClient.getTokenAsObservable()
                .flatMap {token ->
                    val authToken = "${token.bearer} ${token.accessToken}"
                    if (albumName.isBlank()) {
                        appApi.buildDownloadUrl(authToken, remoteAlbumId)
                    } else {
                        var fileName = albumName.toLowerCase().replace(" ", "_")
                        fileName = URLEncoder.encode(fileName, "utf-8")
                        appApi.buildDownloadUrl(authToken, remoteAlbumId, fileName)
                    }
                }
                .map { it.downloadUrl }
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    view.showError(it.message ?: "Error getting download url")
                    view.hideUploadingDialog()
                },
                onSuccess = { downloadUrl ->
                    getFieldsForEmailChooser(downloadUrl)
                }
        ).addTo(disposables)
    }

    /**
     * Gets all the current fields so they can be sent with the body
     */
    fun getFieldsForEmailChooser(downloadUrl: String) {
        albumsRepo.getAlbum(albumKey)
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    view.showError(it.message ?: "Error retrieving album")
                },
                onSuccess = { album ->
                    buildEmailChooser(album.name, album.comments, downloadUrl)
                }
        ).addTo(disposables)
    }

    /**
     * Builds an email chooser with name, comments, and download url
     */
    fun buildEmailChooser(albumName: String, comments: String, downloadUrl: String) {
        val subject = if (albumName.isBlank()) "Secure Image Album"
        else "Secure Image Album: $albumName"

        var body = ""
        if (albumName.isNotBlank()) body += "Album Name:\n$albumName\n\n"
        if (comments.isNotBlank()) body += "Comments:\n$comments\n\n"
        if (downloadUrl.isNotBlank()) body += "Download Images Here:\n$downloadUrl"

        val chooserTitle = "Send download link using..."

        view.hideUploadingDialog()
        view.showEmailChooser(subject, body, chooserTitle)
    }
}
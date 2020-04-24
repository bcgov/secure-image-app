package ca.bc.gov.secureimage.screens.createalbum

import ca.bc.gov.mobileauthentication.MobileAuthenticationClient
import ca.bc.gov.mobileauthentication.data.models.Token
import ca.bc.gov.secureimage.RxImmediateSchedulerRule
import ca.bc.gov.secureimage.common.managers.NetworkManager
import ca.bc.gov.secureimage.data.AppApi
import ca.bc.gov.secureimage.data.models.AddImages
import ca.bc.gov.secureimage.data.models.local.Album
import ca.bc.gov.secureimage.data.models.local.CameraImage
import ca.bc.gov.secureimage.data.models.remote.BuildDownloadUrlResponse
import ca.bc.gov.secureimage.data.models.remote.CreateRemoteAlbumIdResponse
import ca.bc.gov.secureimage.data.repos.albums.AlbumsRepo
import ca.bc.gov.secureimage.data.repos.cameraimages.CameraImagesRepo
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.junit.Assert.*

import org.junit.Before
import org.junit.ClassRule
import org.mockito.internal.verification.Times

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
class CreateAlbumPresenterTest {

    companion object {
        @ClassRule
        @JvmField
        val rxSchedulers = RxImmediateSchedulerRule()
    }

    private lateinit var view: CreateAlbumContract.View

    private val albumKey = "key"

    private lateinit var albumsRepo: AlbumsRepo
    private lateinit var cameraImagesRepo: CameraImagesRepo
    private lateinit var networkManager: NetworkManager
    private lateinit var appApi: AppApi
    private lateinit var mobileAuthenticationClient: MobileAuthenticationClient

    private lateinit var presenter: CreateAlbumPresenter

    @Before
    fun setUp() {
        view = mock()


        albumsRepo = mock()
        cameraImagesRepo = mock()
        networkManager = mock()
        appApi = mock()
        mobileAuthenticationClient = mock()

        presenter = CreateAlbumPresenter(
                view, albumKey, albumsRepo, cameraImagesRepo, networkManager, appApi, mobileAuthenticationClient)
    }

    @After
    fun tearDown() {
        AlbumsRepo.destroyInstance()
        CameraImagesRepo.destroyInstance()
    }

    @Test
    fun presenterSet() {
        verify(view).presenter = presenter
    }

    @Test
    fun subscribe() {
        val album = Album()
        whenever(albumsRepo.getAlbum(albumKey)).thenReturn(Observable.just(album))

        presenter.subscribe()

        verify(view).hideNetworkType()

        verify(view).setBacked(false)
        verify(view).setUpBackListener()

        verify(view).setAlbumDeleted(false)
        verify(view).setUpDeleteAlbumListener()

        verify(view).setRefresh(true)
        verify(view).setUpAddImagesListener()
        verify(view).hideImagesLoading()
        verify(view).setUpImagesList()
        verify(view).setUpViewAllImagesListener()

        verify(view).setUpUploadListener()
    }

    @Test
    fun dispose() {
        presenter.dispose()

        verify(view).hideDeleteAlbumDialog()
        verify(view).hideDeleteImageDialog()
        verify(view).hideDeletingDialog()
        verify(view).hideUploadingDialog()
        verify(view).hideMobileNetworkWarningDialog()
        verify(view).hideNoConnectionDialog()
    }

    @Test
    fun viewShownRefresh() {
        val cameraImages = ArrayList<CameraImage>()
        cameraImages.add(CameraImage())
        whenever(cameraImagesRepo.getAllCameraImagesInAlbum(albumKey))
                .thenReturn(Observable.just(cameraImages))

        presenter.viewShown(true, false)

        verify(view).setBacked(false)

        verify(view, Times(2)).hideAddImagesLayout()
        verify(view).hideViewAllImages()
        verify(view).hideUpload()
        verify(view).showImages(ArrayList())
        verify(view).setRefresh(false)
    }

    @Test
    fun viewShownNoRefresh() {
        presenter.viewShown(false, false)

        verify(view).setBacked(false)
    }

    @Test
    fun networkChangedToWifi() {
        val networkType = NetworkManager.NetworkType.WifiConnection

        presenter.networkTypeChanged(networkType)

        verify(view).hideNetworkType()
        verify(view).clearNetworkTypeText()
    }

    @Test
    fun networkChangedToMobileConnection() {
        val networkType = NetworkManager.NetworkType.MobileConnection

        presenter.networkTypeChanged(networkType)

        verify(view).showNetworkType()
        verify(view).setNetworkTypeTextMobileConnection()
    }

    @Test
    fun networkChangedToNoConnection() {
        val networkType = NetworkManager.NetworkType.NoConnection

        presenter.networkTypeChanged(networkType)

        verify(view).showNetworkType()
        verify(view).setNetworkTypeTextNoConnection()
    }

    @Test
    fun getAlbumFields() {
        val album = Album()
        whenever(albumsRepo.getAlbum(albumKey)).thenReturn(Observable.just(album))

        presenter.getAlbumFields()

        verify(view).setAlbumName(album.name)
    }

    @Test
    fun getImages() {
        val images = ArrayList<CameraImage>()
        images.add(CameraImage())

        val addImages = AddImages()

        val items = ArrayList<Any>()
        items.add(addImages)
        items.addAll(images)

        whenever(cameraImagesRepo.getAllCameraImagesInAlbum(albumKey))
                .thenReturn(Observable.just(images))

        presenter.getImages(addImages)


        verify(view).showImages(ArrayList())
        verify(view).showImagesLoading()

        verify(view).hideImagesLoading()
        verify(view).showImages(items)
    }

    @Test
    fun backClicked() {
        val albumName = "name"
        val comments = "comments"

        presenter.backClicked(false, albumName, comments)

        verify(view).setBacked(true)
    }

    @Test
    fun deleteAlbumClicked() {
        presenter.deleteAlbumClicked()

        verify(view).showDeleteAlbumDialog()
    }

    @Test
    fun viewAllImagesClicked() {
        presenter.viewAllImagesClicked()

        verify(view).setRefresh(true)
        verify(view).goToAllImages(albumKey)
    }

    @Test
    fun getViewAllText() {
        val albumSize = 15

        val expected = "View all Images ($albumSize)"
        val actual = presenter.getViewAllText(albumSize)

        assertEquals(expected, actual)
    }

    @Test
    fun addImagesClicked() {
        presenter.addImagesClicked()

        verify(view).setRefresh(true)
        verify(view).goToSecureCamera(albumKey)
    }

    @Test
    fun imageClicked() {
        val cameraImage = CameraImage()
        val position = 5

        presenter.imageClicked(cameraImage, position)

        verify(view).goToImageDetail(cameraImage.albumKey, position - 1)
    }

    @Test
    fun imageDeleteClicked() {
        val cameraImage = CameraImage()
        val position = 5

        presenter.imageDeleteClicked(cameraImage, position)

        verify(view).showDeleteImageDialog(cameraImage, position)
    }

    @Test
    fun deleteImage() {
        val cameraImage = CameraImage()
        val position = 5

        whenever(cameraImagesRepo.deleteCameraImage(cameraImage))
                .thenReturn(Observable.just(cameraImage))

        whenever(cameraImagesRepo.getCameraImageCountInAlbum(albumKey))
                .thenReturn(Observable.just(10))

        presenter.deleteImage(cameraImage, position)

        verify(view).showImageDeletedMessage()
        verify(view).notifyImageRemoved(cameraImage, position)
    }

    @Test
    fun albumSizeReturnedZero() {
        val albumSize = 0

        presenter.albumSizeReturned(albumSize)

        verify(view).showAddImagesLayout()
        verify(view).hideViewAllImages()
        verify(view).hideUpload()
    }

    @Test
    fun albumSizeReturnedImages() {
        val albumSize = 5

        presenter.albumSizeReturned(albumSize)

        verify(view).hideAddImagesLayout()
        verify(view).showViewAllImages()
        verify(view).setViewAllImagesText(presenter.getViewAllText(albumSize))
        verify(view).showUpload()
    }

    @Test
    fun networkTypeForUploadMobileConnection() {
        val networkType = NetworkManager.NetworkType.MobileConnection

        presenter.checkNetworkTypeForUpload(networkType)

        verify(view).showMobileNetworkWarningDialog()
    }

    @Test
    fun networkTypeForUploadNoConnection() {
        val networkType = NetworkManager.NetworkType.NoConnection

        presenter.checkNetworkTypeForUpload(networkType)

        verify(view).showNoConnectionDialog()
    }

    @Test
    fun upload() {
        val networkType = NetworkManager.NetworkType.WifiConnection

        val imageCount = 5
        val albumId = "abc"
        val remoteAlbumIdResponse = CreateRemoteAlbumIdResponse(albumId)

        val cameraImage = CameraImage()
        val cameraImages = ArrayList<CameraImage>()
        cameraImages.add(cameraImage)

        val downloadUrl = "http://downloadimages.com"
        val buildDownloadUrlResponse = BuildDownloadUrlResponse(downloadUrl)

        val album = Album()
        album.key = albumId
        album.name = "abc123"

        whenever(cameraImagesRepo.getCameraImageCountInAlbum(albumKey))
                .thenReturn(Observable.just(imageCount))

        whenever(appApi.createRemoteAlbumId("bearer 123"))
                .thenReturn(Observable.just(remoteAlbumIdResponse))

        whenever(cameraImagesRepo.getAllCameraImagesInAlbum(albumKey))
                .thenReturn(Observable.just(cameraImages))

        whenever(cameraImagesRepo.uploadCameraImage(albumId, cameraImage))
                .thenReturn(Observable.just(cameraImage))

        whenever(appApi.buildDownloadUrl("bearer 123", albumId))
                .thenReturn(Observable.just(buildDownloadUrlResponse))

        whenever(albumsRepo.getAlbum(albumKey))
                .thenReturn(Observable.just(album))

        whenever(mobileAuthenticationClient.getTokenAsObservable())
                .thenReturn(Observable.just(
                        Token("123", 123123L, 123123L, "",
                                "bearer", "", 123L, "",
                                99999999999999L, 99999999999999L)))

        presenter.checkNetworkTypeForUpload(networkType)

        verify(view).hideUploadingDialog()
    }
}
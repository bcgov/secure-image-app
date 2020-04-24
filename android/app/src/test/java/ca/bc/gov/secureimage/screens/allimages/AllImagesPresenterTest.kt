package ca.bc.gov.secureimage.screens.allimages

import ca.bc.gov.secureimage.RxImmediateSchedulerRule
import ca.bc.gov.secureimage.data.models.AddImages
import ca.bc.gov.secureimage.data.models.local.CameraImage
import ca.bc.gov.secureimage.data.repos.cameraimages.CameraImagesRepo
import com.nhaarman.mockito_kotlin.*
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
class AllImagesPresenterTest {

    companion object {
        @ClassRule
        @JvmField
        val rxSchedulers = RxImmediateSchedulerRule()
    }

    private lateinit var view: AllImagesContract.View

    private val albumKey = "key"

    private lateinit var cameraImagesRepo: CameraImagesRepo

    private lateinit var presenter: AllImagesPresenter

    @Before
    fun setUp() {
        view = mock()

        cameraImagesRepo = mock()

        presenter = AllImagesPresenter(view, albumKey, cameraImagesRepo)
    }

    @After
    fun tearDown() {
        CameraImagesRepo.destroyInstance()
    }

    @Test
    fun presenterSet() {
        verify(view).presenter = presenter
    }

    @Test
    fun subscribe() {
        presenter.subscribe()

        verify(view).hideLoading()

        verify(view).setUpBackListener()
        verify(view).setUpSelectListener()

        verify(view).setUpSelectCloseListener()
        verify(view).setUpSelectDeleteListener()

        verify(view).setRefresh(true)
        verify(view).setUpImagesList()
    }

    @Test
    fun viewShownRefresh() {
        val images = ArrayList<CameraImage>()
        whenever(cameraImagesRepo.getAllCameraImagesInAlbum(albumKey))
                .thenReturn(Observable.just(images))

        presenter.viewShown(true)

        verify(view).setRefresh(false)
    }

    @Test
    fun viewShownNoRefresh() {
        presenter.viewShown(false)

        verify(view).presenter = presenter
        verifyNoMoreInteractions(view)
    }

    @Test
    fun viewHidden() {
        presenter.viewHidden()

        verify(view).hideDeleteImages()
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
        verify(view).showLoading()

        verify(view).hideLoading()
        verify(view).showImages(items)
    }

    @Test
    fun backClicked() {
        presenter.backClicked()

        verify(view).finish()
    }

    @Test
    fun showSelectMode() {
        presenter.showSelectMode()

        verify(view).setToolbarColorPrimaryLight()

        verify(view).hideBack()
        verify(view).hideToolbarTitle()
        verify(view).hideSelect()

        verify(view).showSelectClose()
        verify(view).showSelectTitle()
        verify(view).hideSelectDelete()

        verify(view).setSelectTitleSelectItems()

        verify(view).setSelectMode(true)
    }

    @Test
    fun showToolbarMode() {
        presenter.showToolbarMode()

        verify(view).setToolbarColorPrimary()

        verify(view).showBack()
        verify(view).showToolbarTitle()
        verify(view).showSelect()

        verify(view).hideSelectClose()
        verify(view).hideSelectTitle()
        verify(view).hideSelectDelete()

        verify(view).setSelectMode(false)

        verify(view).clearSelectedImages()
    }

    @Test
    fun selectDeleteClicked() {
        val cameraImages = ArrayList<CameraImage>()

        presenter.selectDeleteClicked(cameraImages)

        verify(view).showDeleteImages(cameraImages)
    }

    @Test
    fun deleteImages() {
        val cameraImages = ArrayList<CameraImage>()

        presenter.deleteImages(cameraImages, false)

        verify(view).showImages(ArrayList())
        verify(view).showLoading()

        verify(view).hideLoading()
        verify(view).showDeletedSuccessfullyMessage()
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
    fun unSelectedImageSelected() {
        val cameraImage = CameraImage()
        cameraImage.selected = false

        val position = 5
        val selectedCount = 3

        presenter.imageSelected(cameraImage, position, selectedCount)

        verify(view).setSelectTitle("${selectedCount + 1} Selected")
        verify(view).showSelectDelete()

        verify(view).itemChanged(position)
    }

    @Test
    fun selectedImageSelected() {
        val cameraImage = CameraImage()
        cameraImage.selected = true

        val position = 5
        val selectedCount = 3

        presenter.imageSelected(cameraImage, position, selectedCount)

        verify(view).setSelectTitle("${selectedCount - 1} Selected")
        verify(view).showSelectDelete()

        verify(view).itemChanged(position)
    }
}
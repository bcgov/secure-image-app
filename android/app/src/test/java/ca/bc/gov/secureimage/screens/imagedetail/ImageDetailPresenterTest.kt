package ca.bc.gov.secureimage.screens.imagedetail

import ca.bc.gov.secureimage.RxImmediateSchedulerRule
import ca.bc.gov.secureimage.data.models.local.CameraImage
import ca.bc.gov.secureimage.data.repos.cameraimages.CameraImagesRepo
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test

import org.junit.Before
import org.junit.ClassRule
import kotlin.math.roundToInt

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
class ImageDetailPresenterTest {

    companion object {
        @ClassRule
        @JvmField
        val rxSchedulers = RxImmediateSchedulerRule()
    }

    private lateinit var view: ImageDetailContract.View

    private val albumKey = "albumKey"

    private val initialImageIndex = 10

    private lateinit var cameraImagesRepo: CameraImagesRepo

    private lateinit var presenter: ImageDetailPresenter

    @Before
    fun setUp() {
        view = mock()

        cameraImagesRepo = mock()

        presenter = ImageDetailPresenter(view, albumKey, initialImageIndex, cameraImagesRepo)
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
        whenever(cameraImagesRepo.getAllCameraImagesInAlbum(albumKey)).thenReturn(Observable.just(ArrayList()))

        presenter.subscribe()

        verify(view).addCloseListener()
        verify(view).setUpImagesList()
        verify(view).addImagesListScrollChangeListener()
    }

    @Test
    fun closeClicked() {
        presenter.closeClicked()

        verify(view).finish()
    }

    @Test
    fun getImages() {
        val images = ArrayList<CameraImage>()
        val items = ArrayList<Any>(images)
        whenever(cameraImagesRepo.getAllCameraImagesInAlbum(albumKey)).thenReturn(Observable.just(ArrayList()))

        presenter.getImages()

        verify(view).showImages(items)
        verify(view).scrollImagesListTo(initialImageIndex)
    }

    @Test
    fun getTitle() {
        val currentIndex = 2
        val imagesSize = 10
        val title = "Image ${currentIndex + 1} of $imagesSize"

        presenter.getTitle(currentIndex, imagesSize)

        verify(view).setToolbarTitle(title)
    }

    @Test
    fun getProgress() {
        val currentIndex = 2
        val imagesSize = 10
        val currentProgress = -1
        val progress = (((currentIndex + 1).toDouble() / imagesSize.toDouble()) * 100).roundToInt()

        presenter.getProgress(currentIndex, imagesSize, currentProgress)

        verify(view).setViewedProgress(progress)
    }

    @Test
    fun getProgressBadParams() {
        val currentIndex = 900
        val imagesSize = 10
        val currentProgress = -1

        presenter.getProgress(currentIndex, imagesSize, currentProgress)

        verify(view).presenter = presenter
        verifyNoMoreInteractions(view)
    }

    @Test
    fun getProgressSameAsCurrent() {
        val currentIndex = 1
        val imagesSize = 10
        val currentProgress = 20

        presenter.getProgress(currentIndex, imagesSize, currentProgress)

        verify(view).presenter = presenter
        verifyNoMoreInteractions(view)
    }

}
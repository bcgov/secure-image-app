package ca.bc.gov.secureimage.screens.albums

import ca.bc.gov.secureimage.RxImmediateSchedulerRule
import ca.bc.gov.secureimage.data.models.Location
import ca.bc.gov.secureimage.data.models.local.Album
import ca.bc.gov.secureimage.data.repos.albums.AlbumsRepo
import ca.bc.gov.secureimage.data.repos.locationrepo.LocationRepo
import com.github.florent37.rxgps.RxGps
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Before
import org.junit.Test

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
class AlbumsPresenterTest {

    companion object {
        @ClassRule
        @JvmField
        val rxSchedulers = RxImmediateSchedulerRule()
    }

    private lateinit var view: AlbumsContract.View

    private lateinit var albumsRepo: AlbumsRepo
    private lateinit var locationRepo: LocationRepo
    private lateinit var rxGps: RxGps

    private lateinit var presenter: AlbumsPresenter

    @Before
    fun setUp() {
        view = mock()

        albumsRepo = mock()
        locationRepo = mock()
        rxGps = mock()

        presenter = AlbumsPresenter(view, albumsRepo, locationRepo, rxGps)
    }

    @After
    fun tearDown() {
        AlbumsRepo.destroyInstance()
        LocationRepo.destroyInstance()
    }

    @Test
    fun presenterSet() {
        verify(view).presenter = presenter
    }

    @Test
    fun subscribe() {
        whenever(locationRepo.getLocation(rxGps, true))
                .thenReturn(Observable.just(Location()))

        presenter.subscribe()

        verify(view).hideLoading()
        verify(view).hideOnboarding()

        verify(view).setUpAlbumsList()
        verify(view).setUpCreateAlbumListener()
    }

    @Test
    fun getAlbums() {
        val albums = ArrayList<Album>()
        albums.add(Album())
        val items = ArrayList<Any>(albums)

        whenever(albumsRepo.getAllAlbums()).thenReturn(Observable.just(albums))

        presenter.getAlbums()

        verify(view).showAlbumItems(ArrayList())
        verify(view).showLoading()

        verify(view).showAlbumItems(items)
        verify(view).hideLoading()
        verify(view).hideOnboarding()
    }

    @Test
    fun getAlbumsEmpty() {
        val albums = ArrayList<Album>()

        whenever(albumsRepo.getAllAlbums()).thenReturn(Observable.just(albums))

        presenter.getAlbums()

        verify(view).showAlbumItems(ArrayList())
        verify(view).showLoading()

        verify(view).hideLoading()
        verify(view).showOnboarding()
    }

    @Test
    fun createAlbum() {
        val album = Album()

        whenever(albumsRepo.createAlbum()).thenReturn(Observable.just(album))

        presenter.createAlbum()

        verify(view).goToCreateAlbum(album.key)
    }

    @Test
    fun albumClicked() {
        val album = Album()

        presenter.albumClicked(album)

        verify(view).goToCreateAlbum(album.key)
    }

}
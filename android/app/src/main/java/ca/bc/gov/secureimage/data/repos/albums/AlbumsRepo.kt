package ca.bc.gov.secureimage.data.repos.albums

import ca.bc.gov.secureimage.data.models.local.Album
import io.reactivex.Observable

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
class AlbumsRepo
private constructor(private val localDataSource: AlbumsDataSource) : AlbumsDataSource {

    companion object {

        @Volatile private var INSTANCE: AlbumsRepo? = null

        fun getInstance(localDataSource: AlbumsDataSource): AlbumsRepo =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: AlbumsRepo(localDataSource).also { INSTANCE = it }
                }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

    override fun createAlbum(): Observable<Album> = localDataSource.createAlbum()

    override fun getAlbum(key: String): Observable<Album> = localDataSource.getAlbum(key)

    override fun getAllAlbums(): Observable<ArrayList<Album>> = localDataSource.getAllAlbums()

    override fun saveAlbum(album: Album): Observable<Album> = localDataSource.saveAlbum(album)

    override fun deleteAlbum(key: String): Observable<Album> = localDataSource.deleteAlbum(key)
}
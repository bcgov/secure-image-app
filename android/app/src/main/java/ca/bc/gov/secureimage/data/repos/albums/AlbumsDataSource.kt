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
interface AlbumsDataSource {

    fun createAlbum(): Observable<Album>

    fun getAlbum(key: String): Observable<Album>

    fun getAllAlbums(): Observable<ArrayList<Album>>

    fun saveAlbum(album: Album): Observable<Album>

    fun deleteAlbum(key: String): Observable<Album>

}
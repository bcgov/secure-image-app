package ca.bc.gov.secureimage.data.repos.albums

import ca.bc.gov.secureimage.data.models.local.Album
import ca.bc.gov.secureimage.data.models.local.CameraImage
import io.reactivex.Observable
import io.realm.Realm
import io.realm.Sort

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
object AlbumsLocalDataSource : AlbumsDataSource {

    override fun createAlbum(): Observable<Album> {
        return Observable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val album = realm.copyFromRealm(realm.copyToRealm(Album()))
                emitter.onNext(album)
            }
            realm.close()

            emitter.onComplete()
        }
    }

    override fun getAlbum(key: String): Observable<Album> {
        return Observable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val album = realm.where(Album::class.java).equalTo("key", key).findFirst()
                if(album != null) emitter.onNext(realm.copyFromRealm(album))
                else emitter.onError(Throwable("Album not found"))
            }
            realm.close()

            emitter.onComplete()
        }
    }

    /**
     * Grabs all albums and also gets the newest camera image in each album for the preview
     */
    override fun getAllAlbums(): Observable<ArrayList<Album>> {
        return Observable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val albums = realm.copyFromRealm(realm.where(Album::class.java).findAll())
                for(album in albums) {
                    val images = realm.where(CameraImage::class.java)
                            .equalTo("albumKey", album.key)
                            .sort("createdTime", Sort.DESCENDING)
                            .findAll()
                    if(images.size > 0) album.previewByteArray = images[0]?.imageByteArray
                }
                emitter.onNext(ArrayList(albums))
            }
            realm.close()

            emitter.onComplete()
        }
    }

    override fun saveAlbum(album: Album): Observable<Album> {
        return Observable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                realm.copyToRealmOrUpdate(album)
                emitter.onNext(album)
            }
            realm.close()

            emitter.onComplete()
        }
    }

    override fun deleteAlbum(key: String): Observable<Album> {
        return Observable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val album = realm.where(Album::class.java).equalTo("key", key).findFirst()
                album?.deleteFromRealm()

                if (album != null) emitter.onNext(album)
                else emitter.onError(Throwable("Album not found"))
            }
            realm.close()

            emitter.onComplete()
        }
    }
}
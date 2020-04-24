package ca.bc.gov.secureimage.data.repos.cameraimages

import ca.bc.gov.mobileauthentication.common.exceptions.InvalidOperationException
import ca.bc.gov.secureimage.data.models.local.CameraImage
import io.reactivex.Observable
import io.realm.Realm

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
object CameraImagesLocalDataSource : CameraImagesDataSource {

    override fun saveCameraImage(cameraImage: CameraImage): Observable<CameraImage> {
        return Observable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                realm.copyToRealmOrUpdate(cameraImage)

                emitter.onNext(cameraImage)
            }
            realm.close()

            emitter.onComplete()
        }
    }

    override fun deleteCameraImage(cameraImage: CameraImage): Observable<CameraImage> {
        return Observable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val cameraImages = realm.where(CameraImage::class.java)
                        .equalTo("key", cameraImage.key).findAll()
                cameraImages.deleteAllFromRealm()

                emitter.onNext(cameraImage)
            }
            realm.close()

            emitter.onComplete()
        }
    }

    override fun getCameraImageCountInAlbum(albumKey: String): Observable<Int> {
        return Observable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val cameraImages = realm.where(CameraImage::class.java)
                        .equalTo("albumKey", albumKey).findAll()

                if(cameraImages != null) emitter.onNext(cameraImages.size)
                else emitter.onNext(0)
            }
            realm.close()

            emitter.onComplete()
        }
    }

    override fun getAllCameraImagesInAlbum(albumKey: String): Observable<ArrayList<CameraImage>> {
        return Observable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val cameraImages = realm.where(CameraImage::class.java)
                        .equalTo("albumKey", albumKey).findAll()

                emitter.onNext(ArrayList(realm.copyFromRealm(cameraImages)))
            }
            realm.close()

            emitter.onComplete()
        }
    }

    override fun deleteAllCameraImagesInAlbum(albumKey: String): Observable<Boolean> {
        return Observable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val cameraImages = realm.where(CameraImage::class.java)
                        .equalTo("albumKey", albumKey).findAll()
                cameraImages.deleteAllFromRealm()

                emitter.onNext(true)
            }
            realm.close()

            emitter.onComplete()
        }
    }

    override fun uploadCameraImage(
            remoteAlbumId: String,
            cameraImage: CameraImage
    ): Observable<CameraImage> = Observable.error(InvalidOperationException())
}
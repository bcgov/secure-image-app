package ca.bc.gov.secureimage.data.repos.cameraimages

import ca.bc.gov.mobileauthentication.MobileAuthenticationClient
import ca.bc.gov.mobileauthentication.common.exceptions.InvalidOperationException
import ca.bc.gov.secureimage.data.AppApi
import ca.bc.gov.secureimage.data.models.local.CameraImage
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.*

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
class CameraImagesRemoteDataSource
private constructor(
        private val appApi: AppApi,
        private val mobileAuthenticationClient: MobileAuthenticationClient
) : CameraImagesDataSource {

    companion object {

        @Volatile private var INSTANCE: CameraImagesRemoteDataSource? = null

        fun getInstance(
                appApi: AppApi,
                mobileAuthenticationClient: MobileAuthenticationClient
        ): CameraImagesRemoteDataSource =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: CameraImagesRemoteDataSource(
                            appApi, mobileAuthenticationClient).also { INSTANCE = it }
                }
    }

    override fun saveCameraImage(cameraImage: CameraImage): Observable<CameraImage> =
            Observable.error(InvalidOperationException())

    override fun deleteCameraImage(cameraImage: CameraImage): Observable<CameraImage> =
            Observable.error(InvalidOperationException())

    override fun getCameraImageCountInAlbum(albumKey: String): Observable<Int> =
            Observable.error(InvalidOperationException())

    override fun getAllCameraImagesInAlbum(albumKey: String): Observable<ArrayList<CameraImage>> =
            Observable.error(InvalidOperationException())

    override fun deleteAllCameraImagesInAlbum(albumKey: String): Observable<Boolean> =
            Observable.error(InvalidOperationException())

    override fun uploadCameraImage(
            remoteAlbumId: String,
            cameraImage: CameraImage
    ): Observable<CameraImage> {

        // Image
        val imageRequestBody = RequestBody.create(
                MediaType.parse("image/*"), cameraImage.imageByteArray)

        val imagePart = MultipartBody.Part.createFormData(
                "file", UUID.randomUUID().toString(), imageRequestBody)

        return mobileAuthenticationClient.getTokenAsObservable()
                .flatMap { token ->
                    val authToken = "${token.bearer} ${token.accessToken}"
                    appApi.uploadImage(authToken, remoteAlbumId, imagePart)
                }
                .flatMap { Observable.just(cameraImage) }
    }
}
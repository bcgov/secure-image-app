package ca.bc.gov.secureimage.data

import ca.bc.gov.secureimage.data.models.remote.CreateRemoteAlbumIdResponse
import ca.bc.gov.secureimage.data.models.remote.BuildDownloadUrlResponse
import ca.bc.gov.secureimage.data.models.remote.UploadImageResponse
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.*

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
interface AppApi {

    // Create remote album
    @POST("/v1/album/")
    fun createRemoteAlbumId(
            @Header("Authorization") authToken: String
    ): Observable<CreateRemoteAlbumIdResponse>

    // Upload image
    @Multipart
    @POST("/v1/album/{remoteAlbumId}/")
    fun uploadImage(
            @Header("Authorization") authToken: String,
            @Path("remoteAlbumId") remoteAlbumId: String,
            @Part imagePart: MultipartBody.Part
    ): Observable<UploadImageResponse>

    // Download url
    @GET("/v1/album/{remoteAlbumId}/")
    fun buildDownloadUrl(
            @Header("Authorization") authToken: String,
            @Path("remoteAlbumId") remoteAlbumId: String,
            @Query("name") name: String? = null
    ): Observable<BuildDownloadUrlResponse>

}
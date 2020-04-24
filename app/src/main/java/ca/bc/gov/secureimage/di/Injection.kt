package ca.bc.gov.secureimage.di

import android.content.Context
import android.net.ConnectivityManager
import ca.bc.gov.mobileauthentication.MobileAuthenticationClient
import ca.bc.gov.secureimage.BuildConfig
import ca.bc.gov.secureimage.common.managers.CompressionManager
import ca.bc.gov.secureimage.common.managers.KeyStorageManager
import ca.bc.gov.secureimage.common.managers.NetworkManager
import ca.bc.gov.secureimage.data.AppApi
import ca.bc.gov.secureimage.data.repos.albums.AlbumsLocalDataSource
import ca.bc.gov.secureimage.data.repos.albums.AlbumsRepo
import ca.bc.gov.secureimage.data.repos.cameraimages.CameraImagesLocalDataSource
import ca.bc.gov.secureimage.data.repos.cameraimages.CameraImagesRemoteDataSource
import ca.bc.gov.secureimage.data.repos.cameraimages.CameraImagesRepo
import ca.bc.gov.secureimage.data.repos.locationrepo.LocationRemoteDataSource
import ca.bc.gov.secureimage.data.repos.locationrepo.LocationRepo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.util.concurrent.TimeUnit

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
object Injection {

    @JvmStatic
    fun provideAlbumsRepo(): AlbumsRepo = AlbumsRepo.getInstance(AlbumsLocalDataSource)

    @JvmStatic
    fun provideCameraImagesRepo(
            appApi: AppApi,
            mobileAuthenticationClient: MobileAuthenticationClient): CameraImagesRepo =
            CameraImagesRepo.getInstance(
                    CameraImagesLocalDataSource,
                    CameraImagesRemoteDataSource.getInstance(appApi, mobileAuthenticationClient))

    @JvmStatic
    fun provideLocationRepo(): LocationRepo = LocationRepo.getInstance(LocationRemoteDataSource)

    @JvmStatic
    fun provideNetworkManager(connectivityManager: ConnectivityManager): NetworkManager =
            NetworkManager(connectivityManager)

    @JvmStatic
    fun provideCompressionManager(): CompressionManager = CompressionManager()

    @JvmStatic
    fun provideKeyStore(): KeyStore = KeyStore.getInstance("AndroidKeyStore")

    @JvmStatic
    fun provideKeyStorageManager(keyStore: KeyStore): KeyStorageManager =
            KeyStorageManager(keyStore)

    // OkHttpClient
    @JvmStatic
    fun provideOkHttpClient(
            readTimeOut: Long,
            connectTimeOut: Long,
            interceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
            .readTimeout(readTimeOut, TimeUnit.SECONDS)
            .connectTimeout(connectTimeOut, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .build()

    // Logging interceptor
    @JvmStatic
    fun provideHttpLoggingInterceptor(
            loggingLevel: HttpLoggingInterceptor.Level
    ): HttpLoggingInterceptor = HttpLoggingInterceptor()
            .apply { level = loggingLevel }

    // Gson
    private var cachedGson: Gson? = null

    @JvmStatic
    fun provideGson(): Gson = cachedGson ?: GsonBuilder()
            .setLenient()
            .create()
            .also { cachedGson = it }

    // Converter Factory
    @JvmStatic
    fun provideConverterFactory(gson: Gson): Converter.Factory = GsonConverterFactory.create(gson)

    // Call Adapter Factory
    private var cachedCallAdapterFactory: CallAdapter.Factory? = null

    @JvmStatic
    fun provideCallAdapterFactory(): CallAdapter.Factory = cachedCallAdapterFactory ?:
            RxJava2CallAdapterFactory.create()
                    .also { cachedCallAdapterFactory = it }

    // Retrofit
    @JvmStatic
    fun provideRetrofit(
            apiDomain: String,
            okHttpClient: OkHttpClient,
            converterFactory: Converter.Factory,
            callAdapterFactory: CallAdapter.Factory
    ): Retrofit = Retrofit.Builder()
            .baseUrl(apiDomain)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()

    // App Api
    @JvmStatic
    fun provideAppApi(retrofit: Retrofit): AppApi = retrofit.create(AppApi::class.java)

    // Mobile auth client
    @JvmStatic
    fun provideMobileAuthenticationClient(context: Context): MobileAuthenticationClient {
        val baseUrl = BuildConfig.SSO_BASE_URL
        val realmName = BuildConfig.SSO_REALM_NAME
        val authEndpoint = BuildConfig.SSO_AUTH_ENDPOINT
        val redirectUri = BuildConfig.SSO_REDIRECT_URI
        val clientId = BuildConfig.SSO_CLIENT_ID

        val mobileAuthenticationClient =
                MobileAuthenticationClient(
                        context, baseUrl, realmName, authEndpoint, redirectUri, clientId)

        return mobileAuthenticationClient
    }
}
package ca.bc.gov.secureimage.di

import ca.bc.gov.secureimage.BuildConfig
import ca.bc.gov.secureimage.common.Constants
import ca.bc.gov.secureimage.data.AppApi
import ca.bc.gov.secureimage.di.Injection
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit

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
object InjectionUtils {

    fun getAppApi(
            gson: Gson = Injection.provideGson(),
            converterFactory: Converter.Factory = Injection.provideConverterFactory(gson),
            callAdapterFactory : CallAdapter.Factory = Injection.provideCallAdapterFactory(),
            readTimeOut: Long = Constants.READ_TIME_OUT,
            connectTimeOut: Long = Constants.CONNECT_TIME_OUT,
            loggingLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY,
            httpLoggingInterceptor: HttpLoggingInterceptor = Injection.provideHttpLoggingInterceptor(
                    loggingLevel),
            okHttpClient: OkHttpClient = Injection.provideOkHttpClient(
                    readTimeOut, connectTimeOut, httpLoggingInterceptor),
            apiDomain: String = BuildConfig.API_URL,
            retrofit: Retrofit = Injection.provideRetrofit(
                    apiDomain, okHttpClient, converterFactory, callAdapterFactory)
    ): AppApi = Injection.provideAppApi(retrofit)

}
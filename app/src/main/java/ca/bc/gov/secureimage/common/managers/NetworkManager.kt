package ca.bc.gov.secureimage.common.managers

import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import android.net.ConnectivityManager
import android.net.NetworkInfo

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
class NetworkManager(
        private val connectivityManager: ConnectivityManager
) {

    sealed class NetworkType {
        object WifiConnection: NetworkType()
        object MobileConnection: NetworkType()
        object NoConnection: NetworkType()
    }

    fun getNetworkTypeListener(initialDelay: Long, period: Long, unit: TimeUnit): Observable<NetworkType> {
        return Observable.interval(initialDelay, period, unit)
                .map { getNetworkType() }
    }

    fun getNetworkType(): NetworkType {
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo

        val isConnected = activeNetwork != null && activeNetwork.isAvailable && activeNetwork.isConnected
        val isWiFi = activeNetwork != null && activeNetwork.type == ConnectivityManager.TYPE_WIFI
        val isMobile = activeNetwork != null && activeNetwork.type == ConnectivityManager.TYPE_MOBILE

        return when {
            isConnected && isWiFi -> NetworkType.WifiConnection
            isConnected && isMobile -> NetworkType.MobileConnection
            else -> NetworkType.NoConnection
        }
    }

}
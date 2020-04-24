package ca.bc.gov.secureimage.data.repos.locationrepo

import ca.bc.gov.secureimage.data.models.Location
import com.github.florent37.rxgps.RxGps
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
class LocationRepo
private constructor(private val remoteDataSource: LocationDataSource) : LocationDataSource {

    companion object {

        @Volatile private var INSTANCE: LocationRepo? = null

        fun getInstance(remoteDataSource: LocationDataSource): LocationRepo =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: LocationRepo(remoteDataSource).also { INSTANCE = it }
                }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

    private var cachedLocation: Location? = null

    fun cacheLocation(location: Location) {
        cachedLocation = location
    }

    override fun getLocation(rxGps: RxGps, cache: Boolean): Observable<Location> {
        return remoteDataSource.getLocation(rxGps, cache)
                .doAfterNext { if (cache) cacheLocation(it) }
    }

    fun getCachedLocation(): Observable<Location> {
        return if(cachedLocation != null) Observable.just(cachedLocation)
        else Observable.empty()
    }

}
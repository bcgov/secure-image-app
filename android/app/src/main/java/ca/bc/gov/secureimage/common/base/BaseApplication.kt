package ca.bc.gov.secureimage.common.base

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import android.os.StrictMode
import android.preference.PreferenceManager
import ca.bc.gov.secureimage.BuildConfig
import com.squareup.leakcanary.LeakCanary
import ca.bc.gov.secureimage.common.managers.KeyStorageManager
import ca.bc.gov.secureimage.di.Injection
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

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
class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        setUpFabric()

        val keyStorageManager = Injection.provideKeyStorageManager(
                Injection.provideKeyStore())

        setUpRealm(keyStorageManager)
        setUpStrictMode()
        setUpLeakCanary()
    }

    // Fabric
    fun setUpFabric() {
        Fabric.with(this, Crashlytics())
    }

    // Realm
    fun setUpRealm(keyStorageManager: KeyStorageManager) {

        Realm.init(this)

        val key = keyStorageManager.getSecureKey(
                "realm",
                64,
                PreferenceManager.getDefaultSharedPreferences(this))

        val config = RealmConfiguration.Builder()
                .encryptionKey(key)
                .build()

        Realm.setDefaultConfiguration(config)

    }

    // Strict mode
    fun setUpStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyDialog()
                    .build())
        }
    }

    // Leak canary
    fun setUpLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }
}
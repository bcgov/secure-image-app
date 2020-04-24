package ca.bc.gov.secureimage.data.models.local

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
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
open class CameraImage : RealmObject() {

    @PrimaryKey
    var key: String = UUID.randomUUID().toString()

    @Index
    var albumKey: String = ""

    var createdTime: Long = System.currentTimeMillis()

    var imageByteArray: ByteArray = ByteArray(64)
    var thumbnailArray: ByteArray = ByteArray(16)

    var lat: Double = 0.0
    var lon: Double = 0.0

    @Ignore
    var selected: Boolean = false

    fun compareTo(cameraImage: CameraImage): Int  = when {
        createdTime > cameraImage.createdTime -> 1
        createdTime < cameraImage.createdTime -> -1
        else -> 0
    }
}
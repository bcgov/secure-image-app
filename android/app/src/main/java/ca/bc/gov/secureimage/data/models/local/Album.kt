package ca.bc.gov.secureimage.data.models.local

import ca.bc.gov.secureimage.common.utils.TimeUtils
import io.realm.RealmObject
import io.realm.annotations.Ignore
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
open class Album : RealmObject() {

    @PrimaryKey
    var key: String = UUID.randomUUID().toString()

    var createdTime: Long = System.currentTimeMillis()
    var updatedTime: Long = System.currentTimeMillis()

    var name: String = ""
    var comments: String = ""

    @Ignore
    var previewByteArray: ByteArray? = null

    fun compareTo(album: Album): Int = when {
        createdTime < album.createdTime -> 1
        createdTime > album.createdTime -> -1
        else -> 0
    }

    fun getDisplayName(): String {
        return if (name.isNotBlank()) name else "Unnamed Album"
    }

    fun getLastSavedTimeString(): String {
        return "Last saved ${TimeUtils.getReadableTime(updatedTime)}"
    }

    fun getCreatedTimeString(): String {
        return "Created ${TimeUtils.getReadableTime(createdTime)}"
    }

}
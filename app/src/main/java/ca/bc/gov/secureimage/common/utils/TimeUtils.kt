package ca.bc.gov.secureimage.common.utils

import java.text.SimpleDateFormat
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
object TimeUtils {

    /**
     * Turns timestamp into a human readable time such as 10 mins ago or just now
     */
    fun getReadableTime(
            timestamp: Long,
            suffix: String = "ago",
            locale: Locale = Locale.getDefault(),
            timeZone: TimeZone = TimeZone.getDefault(),
            dateFormat: String = "MMM d, yyyy"
    ): String {

        // Time units in milliseconds
        val oneSecond = 1000L
        val oneMinute = 60000L
        val oneHour = 3600000L
        val oneDay = 86400000L
        val oneWeek = 604800000L

        val timeDifference = System.currentTimeMillis() - timestamp
        return when {
            timeDifference < oneSecond -> "just now"
            timeDifference < oneMinute -> getTimeString(timeDifference / oneSecond, "sec", suffix)
            timeDifference < oneHour -> getTimeString(timeDifference / oneMinute, "min", suffix)
            timeDifference < oneDay -> getTimeString(timeDifference / oneHour, "hour", suffix)
            timeDifference < oneWeek -> getTimeString(timeDifference / oneDay, "day", suffix)
            else -> getDateString(timestamp, dateFormat, locale, timeZone)
        }
    }

    /**
     * Creates plural version of time unit if time is not equal to 1
     * Appends suffix at end of string
     */
    fun getTimeString(timeValue: Long, timeUnit: String, suffix: String): String {
        var timeString = "$timeValue $timeUnit"
        if (timeValue != 1L) timeString += "s"
        return "$timeString $suffix"
    }

    /**
     * Converts timestamp into specified date format
     */
    fun getDateString(
            timestamp: Long,
            format: String,
            locale: Locale = Locale.getDefault(),
            timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat(format, locale)
        dateFormat.timeZone = timeZone
        return dateFormat.format(date)
    }

}
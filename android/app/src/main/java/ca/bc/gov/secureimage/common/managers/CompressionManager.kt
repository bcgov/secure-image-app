package ca.bc.gov.secureimage.common.managers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.reactivex.Observable
import java.io.ByteArrayOutputStream


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
class CompressionManager {

    fun compressByteArrayAsObservable(
            imageBytes: ByteArray,
            quality: Int,
            reqWidth: Int,
            reqHeight: Int
    ): Observable<ByteArray> = Observable.create<ByteArray> { emitter ->
        val compressedImageBytes = compressByteArray(imageBytes, quality, reqWidth, reqHeight)
        emitter.onNext(compressedImageBytes)
        emitter.onComplete()
    }

    fun compressByteArray(
            imageBytes: ByteArray,
            quality: Int,
            reqWidth: Int,
            reqHeight: Int
    ): ByteArray {

        val options = BitmapFactory.Options()

        // Decode with bounds to get dimensions
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

        // Calculate in sample size
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode without bounds using in sample size
        options.inJustDecodeBounds = false
        val scaledBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

        // Compress
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}
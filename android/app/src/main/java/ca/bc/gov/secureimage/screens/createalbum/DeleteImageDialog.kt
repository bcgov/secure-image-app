package ca.bc.gov.secureimage.screens.createalbum

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import ca.bc.gov.secureimage.R
import ca.bc.gov.secureimage.data.models.local.CameraImage
import kotlinx.android.synthetic.main.dialog_delete_image.*

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
class DeleteImageDialog(
        context: Context?,
        private val deleteListener: DeleteListener,
        private val cameraImage: CameraImage,
        private val position: Int
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_delete_image)

        cancelTv.setOnClickListener {
            hide()
        }

        deleteTv.setOnClickListener {
            deleteListener.deleteImageConfirmed(cameraImage, position)
            hide()
        }
    }

    interface DeleteListener {
        fun deleteImageConfirmed(cameraImage: CameraImage, position: Int)
    }
}
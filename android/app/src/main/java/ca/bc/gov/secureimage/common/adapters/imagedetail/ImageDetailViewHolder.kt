package ca.bc.gov.secureimage.common.adapters.imagedetail

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.bc.gov.secureimage.R
import ca.bc.gov.secureimage.data.models.local.CameraImage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.item_image_detail.view.*

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
class ImageDetailViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    fun bind(cameraImage: CameraImage) = with(itemView) {

        // Image
        Glide.with(context)
                .load(cameraImage.imageByteArray)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageIv)

    }

    companion object {
        fun create(
                inflater: LayoutInflater,
                root: ViewGroup?,
                attachToRoot: Boolean = false
        ): ImageDetailViewHolder =
                ImageDetailViewHolder(inflater.inflate(R.layout.item_image_detail, root, attachToRoot))
    }

}
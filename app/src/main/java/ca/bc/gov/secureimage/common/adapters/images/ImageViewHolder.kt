package ca.bc.gov.secureimage.common.adapters.images

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.bc.gov.secureimage.R
import ca.bc.gov.secureimage.data.models.local.CameraImage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.item_image.view.*

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
class ImageViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    fun bind(
            cameraImage: CameraImage,
            showDelete: Boolean,
            selectMode: Boolean,
            imageClickListener: ImageClickListener
    ) = with(itemView) {

        // Image
        Glide.with(context)
                .load(cameraImage.thumbnailArray)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageIv)

        // Show delete
        if (showDelete) {
            deleteIv.visibility = View.VISIBLE
        } else {
            deleteIv.visibility = View.GONE
        }

        // Select mode
        if (selectMode) {
            selectedIv.visibility = View.VISIBLE
        } else {
            selectedIv.visibility = View.GONE
        }

        if(cameraImage.selected) {
            selectedIv.setImageResource(R.drawable.ic_radio_button_checked_black_24dp)
            selectedIv.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent))
        } else {
            selectedIv.setImageResource(R.drawable.ic_radio_button_unchecked_black_24dp)
            selectedIv.setColorFilter(ContextCompat.getColor(context, R.color.lightGray))
        }

        // Clicks
        layout.setOnClickListener {
            if(selectMode) {
                imageClickListener.imageSelected(cameraImage, adapterPosition)
            } else {
                imageClickListener.imageClicked(cameraImage, adapterPosition)
            }
        }

        layout.setOnLongClickListener {
            if(!selectMode) {
                imageClickListener.imageLongClicked(cameraImage, adapterPosition)
            }
            true
        }

        deleteIv.setOnClickListener {
            if (showDelete) {
                imageClickListener.imageDeleteClicked(cameraImage, adapterPosition)
            }
        }

    }

    companion object {
        fun create(
                inflater: LayoutInflater,
                root: ViewGroup?,
                attachToRoot: Boolean = false
        ): ImageViewHolder =
                ImageViewHolder(inflater.inflate(R.layout.item_image, root, attachToRoot))
    }

    interface ImageClickListener {
        fun imageClicked(cameraImage: CameraImage, position: Int)
        fun imageSelected(cameraImage: CameraImage, position: Int)
        fun imageLongClicked(cameraImage: CameraImage, position: Int)
        fun imageDeleteClicked(cameraImage: CameraImage, position: Int)
    }

}
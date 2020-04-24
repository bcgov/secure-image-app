package ca.bc.gov.secureimage.common.adapters.images

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ca.bc.gov.secureimage.data.models.AddImages
import ca.bc.gov.secureimage.data.models.local.CameraImage

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
class ImagesAdapter(
        private val inflater: LayoutInflater,
        private val addImagesListener: AddImagesViewHolder.Listener,
        private val imageClickListener : ImageViewHolder.ImageClickListener,
        private val showDelete: Boolean,
        private var items: ArrayList<Any> = ArrayList(),
        private var selectMode: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is CameraImage -> CAMERA_IMAGE
        is AddImages -> ADD_IMAGES
        else -> -1
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        return when (viewType) {
            CAMERA_IMAGE -> ImageViewHolder.create(inflater, parent)
            ADD_IMAGES -> AddImagesViewHolder.create(inflater, parent)
            else -> null
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is ImageViewHolder -> holder.bind(items[position] as CameraImage, showDelete,
                    selectMode, imageClickListener)
            is AddImagesViewHolder -> holder.bind(addImagesListener)
        }
    }

    override fun getItemCount(): Int = items.size

    fun replaceItems(items: ArrayList<Any>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun notifyImageRemoved(cameraImage: CameraImage, position: Int) {
        items.remove(cameraImage)
        notifyItemRemoved(position)
    }

    fun setSelectMode(selectMode: Boolean) {
        this.selectMode = selectMode
        notifyDataSetChanged()
    }

    fun getSelectedCount(): Int {
        var count = 0
        items.forEach { if(it is CameraImage && it.selected) count++ }
        return count
    }

    fun getSelectedImages(): ArrayList<CameraImage> {
        val images = ArrayList<CameraImage>()
        items.forEach { if(it is CameraImage && it.selected) images.add(it) }
        return images
    }

    fun clearSelectedImages() {
        items.forEach { if(it is CameraImage) it.selected = false }
    }

    companion object {
        val CAMERA_IMAGE = 0
        val ADD_IMAGES = 1
    }
}
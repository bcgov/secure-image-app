package ca.bc.gov.secureimage.common.adapters.images

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.bc.gov.secureimage.R
import kotlinx.android.synthetic.main.item_add_images.view.*

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
class AddImagesViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    fun bind(listener: Listener) = with(itemView) {
        layout.setOnClickListener {
            listener.addImagesClicked()
        }
    }

    companion object {
        fun create(
                inflater: LayoutInflater,
                root: ViewGroup?,
                attachToRoot: Boolean = false
        ): AddImagesViewHolder =
                AddImagesViewHolder(inflater.inflate(R.layout.item_add_images, root, attachToRoot))
    }

    interface Listener {
        fun addImagesClicked()
    }

}
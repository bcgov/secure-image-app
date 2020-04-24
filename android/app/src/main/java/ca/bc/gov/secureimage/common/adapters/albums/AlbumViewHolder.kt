package ca.bc.gov.secureimage.common.adapters.albums

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.bc.gov.secureimage.R
import ca.bc.gov.secureimage.data.models.local.Album
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.item_album.view.*

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
class AlbumViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    fun bind(album: Album, clickListener: ClickListener) = with(itemView) {

        // Image
        val imageBytes = album.previewByteArray
        Glide.with(context)
                .load(imageBytes)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageIv)

        if (imageBytes != null) {
            imagePlaceholderIv.visibility = View.GONE
        } else {
            imagePlaceholderIv.visibility = View.VISIBLE
        }

        // Name
        nameTv.text = album.getDisplayName()

        // Created time
        createdTimeTv.text = album.getCreatedTimeString()

        // Updated time
        updatedTimeTv.text = album.getLastSavedTimeString()

        // Clicks
        layout.setOnClickListener {
            clickListener.albumClicked(album)
        }
    }

    companion object {
        fun create(
                inflater: LayoutInflater,
                root: ViewGroup?,
                attachToRoot: Boolean = false
        ): AlbumViewHolder =
                AlbumViewHolder(inflater.inflate(R.layout.item_album, root, attachToRoot))
    }

    interface ClickListener {
        fun albumClicked(album: Album)
    }

}
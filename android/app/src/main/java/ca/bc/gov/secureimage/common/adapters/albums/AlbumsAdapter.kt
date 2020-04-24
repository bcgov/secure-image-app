package ca.bc.gov.secureimage.common.adapters.albums

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ca.bc.gov.secureimage.data.models.local.Album

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
class AlbumsAdapter(
        private val inflater: LayoutInflater,
        private val albumClickListener: AlbumViewHolder.ClickListener,
        private var items: ArrayList<Any> = ArrayList()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is Album -> ALBUM
        else -> -1
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        return when (viewType) {
            ALBUM -> AlbumViewHolder.create(inflater, parent)
            else -> null
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is AlbumViewHolder -> holder.bind(items[position] as Album, albumClickListener)
        }
    }

    override fun getItemCount(): Int = items.size

    fun replaceItems(items: ArrayList<Any>) {
        this.items = items
        notifyDataSetChanged()
    }

    companion object {
        val ALBUM = 0
    }

}
package ca.bc.gov.secureimage.screens.albums

import ca.bc.gov.secureimage.common.base.BasePresenter
import ca.bc.gov.secureimage.common.base.BaseView
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
interface AlbumsContract {

    interface View: BaseView<Presenter> {
        fun showError(message: String)

        fun showLoading()
        fun hideLoading()

        fun setUpAlbumsList()
        fun showAlbumItems(items: ArrayList<Any>)

        fun setUpCreateAlbumListener()
        fun goToCreateAlbum(albumKey: String)

        fun showOnboarding()
        fun hideOnboarding()
    }

    interface Presenter: BasePresenter {
        fun viewShown()
        fun viewHidden()

        fun createAlbumClicked()

        fun albumClicked(album: Album)
    }

}
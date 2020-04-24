package ca.bc.gov.secureimage.screens.imagedetail

import ca.bc.gov.secureimage.common.base.BasePresenter
import ca.bc.gov.secureimage.common.base.BaseView

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
interface ImageDetailContract {

    interface View : BaseView<Presenter> {
        fun finish()

        fun showError(message: String)

        fun addCloseListener()

        fun setToolbarTitle(title: String)

        fun setUpImagesList()
        fun addImagesListScrollChangeListener()
        fun showImages(items: ArrayList<Any>)
        fun scrollImagesListTo(position: Int)

        fun setViewedProgress(progress: Int)
    }

    interface Presenter : BasePresenter {
        fun closeClicked()

        fun imagesListScrollChanged(currentIndex: Int, imagesSize: Int, currentProgress: Int)
    }

}
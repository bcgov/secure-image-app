package ca.bc.gov.secureimage.screens.allimages

import ca.bc.gov.secureimage.common.base.BasePresenter
import ca.bc.gov.secureimage.common.base.BaseView
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
interface AllImagesContract {

    interface View: BaseView<Presenter> {
        fun finish()

        fun setRefresh(refresh: Boolean)

        fun showLoading()
        fun hideLoading()

        fun showDeletedSuccessfullyMessage()
        fun showError(message: String)

        fun setToolbarColorPrimary()
        fun setToolbarColorPrimaryLight()

        fun showBack()
        fun hideBack()
        fun setUpBackListener()

        fun showToolbarTitle()
        fun hideToolbarTitle()

        fun showSelect()
        fun hideSelect()
        fun setUpSelectListener()

        fun showSelectClose()
        fun hideSelectClose()
        fun setUpSelectCloseListener()

        fun showSelectTitle()
        fun hideSelectTitle()
        fun setSelectTitleSelectItems()
        fun setSelectTitle(title: String)

        fun showSelectDelete()
        fun hideSelectDelete()
        fun setUpSelectDeleteListener()

        fun showDeleteImages(cameraImages: ArrayList<CameraImage>)
        fun hideDeleteImages()

        fun setUpImagesList()
        fun showImages(items: ArrayList<Any>)
        fun setSelectMode(selectMode: Boolean)
        fun itemChanged(position: Int)
        fun clearSelectedImages()

        fun goToSecureCamera(albumKey: String)
        fun goToImageDetail(albumKey: String, imageIndex: Int)
    }

    interface Presenter: BasePresenter {
        fun viewShown(refresh: Boolean)
        fun viewHidden()

        fun backClicked()

        fun selectClicked()

        fun closeSelectClicked()

        fun selectDeleteClicked(cameraImages: ArrayList<CameraImage>)

        fun deleteImagesConfirmed(cameraImages: ArrayList<CameraImage>)

        fun addImagesClicked()

        fun imageClicked(cameraImage: CameraImage, position: Int)

        fun imageSelected(cameraImage: CameraImage, position: Int, selectedCount: Int)

        fun imageLongClicked(cameraImage: CameraImage, position: Int, selectedCount: Int)
    }

}
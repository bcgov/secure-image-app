package ca.bc.gov.secureimage.screens.createalbum

import ca.bc.gov.mobileauthentication.MobileAuthenticationClient
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
interface CreateAlbumContract {

    interface View: BaseView<Presenter> {
        fun finish()

        fun setBacked(backed: Boolean)
        fun setRefresh(refresh: Boolean)
        fun setAlbumDeleted(albumDeleted: Boolean)

        fun showAlbumDeletedMessage()
        fun showImageDeletedMessage()
        fun showError(message: String)

        fun setUpBackListener()

        fun showNetworkType()
        fun hideNetworkType()
        fun clearNetworkTypeText()
        fun setNetworkTypeTextMobileConnection()
        fun setNetworkTypeTextNoConnection()

        fun setUpAddImagesListener()
        fun showAddImagesLayout()
        fun hideAddImagesLayout()
        fun goToSecureCamera(albumKey: String)

        fun showImagesLoading()
        fun hideImagesLoading()

        fun setUpImagesList()
        fun showImages(items: ArrayList<Any>)
        fun notifyImageRemoved(cameraImage: CameraImage, position: Int)

        fun setUpViewAllImagesListener()
        fun showViewAllImages()
        fun hideViewAllImages()
        fun setViewAllImagesText(text: String)
        fun goToAllImages(albumKey: String)
        fun goToImageDetail(albumKey: String, imageIndex: Int)

        fun showDeleteImageDialog(cameraImage: CameraImage, position: Int)
        fun hideDeleteImageDialog()

        fun setUpDeleteAlbumListener()
        fun showDeleteAlbumDialog()
        fun hideDeleteAlbumDialog()

        fun showDeletingDialog()
        fun hideDeletingDialog()

        fun setAlbumName(albumName: String)

        fun setComments(comments: String)

        fun showUpload()
        fun hideUpload()
        fun setUpUploadListener()

        fun showUploadingDialog(maxUploadCount: Int)
        fun hideUploadingDialog()
        fun incrementUploadedCount()

        fun showMobileNetworkWarningDialog()
        fun hideMobileNetworkWarningDialog()

        fun showNoConnectionDialog()
        fun hideNoConnectionDialog()

        fun showEmailChooser(subject: String, body: String, chooserTitle: String)
    }

    interface Presenter: BasePresenter {
        val mobileAuthenticationClient: MobileAuthenticationClient

        fun viewShown(refresh: Boolean, addNetworkListener: Boolean)
        fun viewHidden(backed: Boolean, albumDeleted: Boolean, albumName: String, comments: String)

        fun backClicked(saveAlbum: Boolean = false, albumName: String, comments: String)

        fun deleteAlbumClicked()
        fun deleteAlbumConfirmed()

        fun viewAllImagesClicked()

        fun addImagesClicked()

        fun imageClicked(cameraImage: CameraImage, position: Int)

        fun imageDeleteClicked(cameraImage: CameraImage, position: Int)

        fun deleteImageConfirmed(cameraImage: CameraImage, position: Int)

        fun uploadClicked(albumName: String, comments: String)

        fun uploadAnywayClicked()

        fun authenticationSuccess()
    }

}
package ca.bc.gov.secureimage.screens.createalbum

import ca.bc.gov.secureimage.common.base.BasePresenter
import ca.bc.gov.secureimage.common.base.BaseView

/**
 * Created by Aidan Laing on 2017-12-12.
 *
 */
interface CreateAlbumContract {

    interface View: BaseView<Presenter> {
        fun finish()

        fun showError(message: String)
        fun showMessage(message: String)

        fun setUpBackListener()

        fun setUpImagesList()
        fun showImages(items: ArrayList<Any>)

        fun setUpViewAllImagesListener()
        fun showViewAllImages()
        fun hideViewAllImages()
        fun goToAllImages(albumKey: String)

        fun setUpSaveListener()

        fun goToSecureCamera(albumKey: String)
    }

    interface Presenter: BasePresenter {
        fun viewShown()
        fun viewHidden()

        fun backClicked()

        fun saveClicked(albumName: String)

        fun viewAllImagesClicked()

        fun addImagesClicked()
    }

}
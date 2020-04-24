package ca.bc.gov.secureimage.screens.createalbum

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import ca.bc.gov.mobileauthentication.MobileAuthenticationClient
import ca.bc.gov.mobileauthentication.data.models.Token
import ca.bc.gov.secureimage.di.Injection
import ca.bc.gov.secureimage.screens.securecamera.SecureCameraActivity
import ca.bc.gov.secureimage.R
import ca.bc.gov.secureimage.common.Constants
import ca.bc.gov.secureimage.common.adapters.images.AddImagesViewHolder
import ca.bc.gov.secureimage.common.adapters.images.ImageViewHolder
import ca.bc.gov.secureimage.common.adapters.images.ImagesAdapter
import ca.bc.gov.secureimage.di.InjectionUtils
import ca.bc.gov.secureimage.data.models.local.CameraImage
import ca.bc.gov.secureimage.screens.allimages.AllImagesActivity
import ca.bc.gov.secureimage.screens.imagedetail.ImageDetailActivity
import kotlinx.android.synthetic.main.activity_create_album.*

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
class CreateAlbumActivity : AppCompatActivity(), CreateAlbumContract.View,
        AddImagesViewHolder.Listener, ImageViewHolder.ImageClickListener,
        DeleteAlbumDialog.DeleteListener, DeleteImageDialog.DeleteListener, MobileNetworkWarningDialog.UploadListener {

    override var presenter: CreateAlbumContract.Presenter? = null

    private var imagesAdapter: ImagesAdapter? = null

    private var deleteAlbumDialog: DeleteAlbumDialog? = null

    private var deleteImageDialog: DeleteImageDialog? = null

    private var deletingDialog: DeletingDialog? = null

    private var uploadingDialog: UploadingDialog? = null

    private var mobileNetworkWarningDialog: MobileNetworkWarningDialog? = null

    private var noConnectionDialog: NoConnectionDialog? = null

    private var backed = false

    private var refresh = true

    private var albumDeleted = false

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_album)

        // Extras
        val albumKey = intent.getStringExtra(Constants.ALBUM_KEY)
        if(albumKey == null) {
            showError("No Album key passed")
            finish()
            return
        }

        val appApi = InjectionUtils.getAppApi()

        val networkManager = Injection.provideNetworkManager(
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

        val mobileAuthenticationClient = Injection.provideMobileAuthenticationClient(this)

        CreateAlbumPresenter(
                this,
                albumKey,
                Injection.provideAlbumsRepo(),
                Injection.provideCameraImagesRepo(appApi, mobileAuthenticationClient),
                networkManager,
                appApi,
                mobileAuthenticationClient)

        presenter?.subscribe()
    }

    override fun onResume() {
        super.onResume()
        presenter?.viewShown(refresh, true)
    }

    override fun onPause() {
        super.onPause()
        val albumName = albumNameEt.text.toString()
        val comments = commentsEt.text.toString()
        presenter?.viewHidden(backed, albumDeleted, albumName, comments)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter?.mobileAuthenticationClient?.handleAuthResult(requestCode, resultCode, data, object : MobileAuthenticationClient.TokenCallback {
            override fun onError(throwable: Throwable) {
                showToast(throwable.message ?: "Error logging in")
            }

            override fun onSuccess(token: Token) {
                presenter?.authenticationSuccess()
            }
        })
    }

    // Setters
    override fun setBacked(backed: Boolean) {
        this.backed = backed
    }

    override fun setRefresh(refresh: Boolean) {
        this.refresh = refresh
    }

    override fun setAlbumDeleted(albumDeleted: Boolean) {
        this.albumDeleted = albumDeleted
    }

    // Messages
    override fun showAlbumDeletedMessage() {
        showToast(getString(R.string.album_deleted))
    }

    override fun showImageDeletedMessage() {
        showToast(getString(R.string.image_deleted))
    }

    override fun showError(message: String) {
        showToast(message)
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Back
    override fun onBackPressed() {
        backEvent()
    }

    override fun setUpBackListener() {
        backIv.setOnClickListener {
            backEvent()
        }
    }

    fun backEvent() {
        val albumName = albumNameEt.text.toString()
        val comments = commentsEt.text.toString()
        presenter?.backClicked(true, albumName, comments)
    }

    // Network type
    override fun showNetworkType() {
        networkTypeTv.visibility = View.VISIBLE
    }

    override fun hideNetworkType() {
        networkTypeTv.visibility = View.GONE
    }

    override fun clearNetworkTypeText() {
        networkTypeTv.text = null
    }

    override fun setNetworkTypeTextMobileConnection() {
        networkTypeTv.setText(R.string.mobile_connection)
    }

    override fun setNetworkTypeTextNoConnection() {
        networkTypeTv.setText(R.string.no_internet_connection)
    }

    // Add images
    override fun setUpAddImagesListener() {
        addImagesLayout.setOnClickListener {
            presenter?.addImagesClicked()
        }
    }

    override fun showAddImagesLayout() {
        addImagesLayout.visibility = View.VISIBLE
    }

    override fun hideAddImagesLayout() {
        addImagesLayout.visibility = View.GONE
    }

    override fun goToSecureCamera(albumKey: String) {
        Intent(this, SecureCameraActivity::class.java)
                .putExtra(Constants.ALBUM_KEY, albumKey)
                .run { startActivity(this) }
    }

    // Image loading
    override fun showImagesLoading() {
        imagesProgressBar.visibility = View.VISIBLE
    }

    override fun hideImagesLoading() {
        imagesProgressBar.visibility = View.GONE
    }

    // Image list
    override fun setUpImagesList() {
        imagesAdapter = ImagesAdapter(LayoutInflater.from(this), this,
                this, true)
        imagesRv.apply {
            layoutManager = NoScrollGridLayoutManager(this@CreateAlbumActivity, 3)
            adapter = imagesAdapter
        }
    }

    override fun addImagesClicked() {
        presenter?.addImagesClicked()
    }

    override fun imageClicked(cameraImage: CameraImage, position: Int) {
        presenter?.imageClicked(cameraImage, position)
    }

    override fun imageSelected(cameraImage: CameraImage, position: Int) {

    }

    override fun imageLongClicked(cameraImage: CameraImage, position: Int) {

    }

    override fun imageDeleteClicked(cameraImage: CameraImage, position: Int) {
        presenter?.imageDeleteClicked(cameraImage, position)
    }

    override fun showImages(items: ArrayList<Any>) {
        imagesAdapter?.replaceItems(items)
    }

    override fun notifyImageRemoved(cameraImage: CameraImage, position: Int) {
        imagesAdapter?.notifyImageRemoved(cameraImage, position)
    }

    override fun goToImageDetail(albumKey: String, imageIndex: Int) {
        Intent(this, ImageDetailActivity::class.java)
                .putExtra(Constants.ALBUM_KEY, albumKey)
                .putExtra(Constants.IMAGE_INDEX, imageIndex)
                .run { startActivity(this) }
    }

    // View all images
    override fun setUpViewAllImagesListener() {
        viewAllImagesTv.setOnClickListener {
            presenter?.viewAllImagesClicked()
        }
    }

    override fun showViewAllImages() {
        viewAllImagesTv.visibility = View.VISIBLE
    }

    override fun hideViewAllImages() {
        viewAllImagesTv.visibility = View.GONE
    }

    override fun setViewAllImagesText(text: String) {
        viewAllImagesTv.text = text
    }

    override fun goToAllImages(albumKey: String) {
        Intent(this, AllImagesActivity::class.java)
                .putExtra(Constants.ALBUM_KEY, albumKey)
                .run { startActivity(this) }
    }

    // Image deletion confirmation
    override fun showDeleteImageDialog(cameraImage: CameraImage, position: Int) {
        deleteImageDialog = DeleteImageDialog(this, this, cameraImage, position)
        deleteImageDialog?.show()
    }

    override fun hideDeleteImageDialog() {
        deleteImageDialog?.hide()
    }

    override fun deleteImageConfirmed(cameraImage: CameraImage, position: Int) {
        presenter?.deleteImageConfirmed(cameraImage, position)
    }

    // Delete album
    override fun setUpDeleteAlbumListener() {
        deleteAlbumIv.setOnClickListener {
            presenter?.deleteAlbumClicked()
        }
    }

    override fun showDeleteAlbumDialog() {
        if (deleteAlbumDialog == null) deleteAlbumDialog = DeleteAlbumDialog(this, this)
        deleteAlbumDialog?.show()
    }

    override fun hideDeleteAlbumDialog() {
        deleteAlbumDialog?.hide()
    }

    override fun deleteAlbumConfirmed() {
        presenter?.deleteAlbumConfirmed()
    }

    // Deleting dialog
    override fun showDeletingDialog() {
        deletingDialog = DeletingDialog(this)
        deletingDialog?.show()
    }

    override fun hideDeletingDialog() {
        deletingDialog?.hide()
    }

    // Album name
    override fun setAlbumName(albumName: String) {
        albumNameEt.setText(albumName)
    }

    // Comments
    override fun setComments(comments: String) {
        commentsEt.setText(comments)
    }

    // Upload
    override fun showUpload() {
        uploadTv.visibility = View.VISIBLE
    }

    override fun hideUpload() {
        uploadTv.visibility = View.GONE
    }

    override fun setUpUploadListener() {
        uploadTv.setOnClickListener {
            val albumName = albumNameEt.text.toString()
            val comments = commentsEt.text.toString()
            presenter?.uploadClicked(albumName, comments)
        }
    }

    // Uploading
    override fun showUploadingDialog(maxUploadCount: Int) {
        uploadingDialog = UploadingDialog(this, maxUploadCount)
        uploadingDialog?.show()
    }

    override fun hideUploadingDialog() {
        uploadingDialog?.hide()
    }

    override fun incrementUploadedCount() {
        uploadingDialog?.incrementUploadedCount()
    }

    // Mobile network
    override fun showMobileNetworkWarningDialog() {
        mobileNetworkWarningDialog = MobileNetworkWarningDialog(this, this)
        mobileNetworkWarningDialog?.show()
    }

    override fun hideMobileNetworkWarningDialog() {
        mobileNetworkWarningDialog?.hide()
    }

    override fun uploadAnywayClicked() {
        presenter?.uploadAnywayClicked()
    }

    // No connection
    override fun showNoConnectionDialog() {
        noConnectionDialog = NoConnectionDialog(this)
        noConnectionDialog?.show()
    }

    override fun hideNoConnectionDialog() {
        noConnectionDialog?.hide()
    }

    // Email
    override fun showEmailChooser(
            subject: String,
            body: String,
            chooserTitle: String) {

        ShareCompat.IntentBuilder.from(this)
                .setType("message/rfc822")
                .setSubject(subject)
                .setText(body)
                .setChooserTitle(chooserTitle)
                .startChooser()
    }
}

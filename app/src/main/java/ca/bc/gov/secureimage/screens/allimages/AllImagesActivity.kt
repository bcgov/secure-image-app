package ca.bc.gov.secureimage.screens.allimages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import ca.bc.gov.secureimage.di.Injection
import ca.bc.gov.secureimage.screens.securecamera.SecureCameraActivity
import ca.bc.gov.secureimage.R
import ca.bc.gov.secureimage.common.Constants
import ca.bc.gov.secureimage.common.adapters.images.AddImagesViewHolder
import ca.bc.gov.secureimage.common.adapters.images.ImageViewHolder
import ca.bc.gov.secureimage.common.adapters.images.ImagesAdapter
import ca.bc.gov.secureimage.di.InjectionUtils
import ca.bc.gov.secureimage.data.models.local.CameraImage
import ca.bc.gov.secureimage.screens.imagedetail.ImageDetailActivity
import kotlinx.android.synthetic.main.activity_all_images.*

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
class AllImagesActivity : AppCompatActivity(), AllImagesContract.View, AddImagesViewHolder.Listener, ImageViewHolder.ImageClickListener, DeleteImagesDialog.DeleteListener {

    override var presenter: AllImagesContract.Presenter? = null

    private var imagesAdapter: ImagesAdapter? = null

    private var deleteImagesDialog: DeleteImagesDialog? = null

    private var refresh = true

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_images)

        // Extras
        val albumKey = intent.getStringExtra(Constants.ALBUM_KEY)
        if(albumKey == null) {
            showError("No Album key passed")
            finish()
            return
        }
        val mobileAuthenticationClient = Injection.provideMobileAuthenticationClient(this)

        AllImagesPresenter(this,
                albumKey,
                Injection.provideCameraImagesRepo(
                        InjectionUtils.getAppApi(),
                        mobileAuthenticationClient))

        presenter?.subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.dispose()
    }

    override fun onResume() {
        super.onResume()
        presenter?.viewShown(refresh)
    }

    override fun onPause() {
        super.onPause()
        presenter?.viewHidden()
    }

    // Refresh
    override fun setRefresh(refresh: Boolean) {
        this.refresh = refresh
    }

    // Loading
    override fun showLoading() {
        viewedProgressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        viewedProgressBar.visibility = View.GONE
    }

    // Message
    override fun showDeletedSuccessfullyMessage() {
        showToast(getString(R.string.deleted_successfully))
    }

    // Error
    override fun showError(message: String) {
        showToast(message)
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Toolbar color
    override fun setToolbarColorPrimary() {
        toolbarBackgroundView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
    }

    override fun setToolbarColorPrimaryLight() {
        toolbarBackgroundView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight))
    }

    // Back
    override fun showBack() {
        backIv.visibility = View.VISIBLE
    }

    override fun hideBack() {
        backIv.visibility = View.GONE
    }

    override fun setUpBackListener() {
        backIv.setOnClickListener {
            presenter?.backClicked()
        }
    }

    // Toolbar title
    override fun showToolbarTitle() {
        toolbarTitleTv.visibility = View.VISIBLE
    }

    override fun hideToolbarTitle() {
        toolbarTitleTv.visibility = View.GONE
    }

    // Select
    override fun showSelect() {
        selectTv.visibility = View.VISIBLE
    }

    override fun hideSelect() {
        selectTv.visibility = View.GONE
    }

    override fun setUpSelectListener() {
        selectTv.setOnClickListener {
            presenter?.selectClicked()
        }
    }

    // Close select
    override fun showSelectClose() {
        toolbarSelectCloseIv.visibility = View.VISIBLE
    }

    override fun hideSelectClose() {
        toolbarSelectCloseIv.visibility = View.GONE
    }

    override fun setUpSelectCloseListener() {
        toolbarSelectCloseIv.setOnClickListener {
            presenter?.closeSelectClicked()
        }
    }

    // Select title
    override fun showSelectTitle() {
        toolbarSelectTitleTv.visibility = View.VISIBLE
    }

    override fun hideSelectTitle() {
        toolbarSelectTitleTv.visibility = View.GONE
    }

    override fun setSelectTitle(title: String) {
        toolbarSelectTitleTv.text = title
    }

    override fun setSelectTitleSelectItems() {
        toolbarSelectTitleTv.setText(R.string.select_items)
    }

    // Select delete
    override fun showSelectDelete() {
        deleteTv.visibility = View.VISIBLE
    }

    override fun hideSelectDelete() {
        deleteTv.visibility = View.GONE
    }

    override fun setUpSelectDeleteListener() {
        deleteTv.setOnClickListener {
            presenter?.selectDeleteClicked(imagesAdapter?.getSelectedImages() ?: ArrayList())
        }
    }

    // Delete images confirmation
    override fun showDeleteImages(cameraImages: ArrayList<CameraImage>) {
        deleteImagesDialog = DeleteImagesDialog(this, this, cameraImages)
        deleteImagesDialog?.show()
    }

    override fun hideDeleteImages() {
        deleteImagesDialog?.dismiss()
    }

    override fun deleteImagesConfirmed(cameraImages: ArrayList<CameraImage>) {
        presenter?.deleteImagesConfirmed(cameraImages)
    }

    // Images list
    override fun setUpImagesList() {
        imagesAdapter = ImagesAdapter(LayoutInflater.from(this), this,
                this, false)
        imagesRv.apply {
            layoutManager = GridLayoutManager(this@AllImagesActivity, 3)
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
        presenter?.imageSelected(cameraImage, position, imagesAdapter?.getSelectedCount() ?: 0)
    }

    override fun imageLongClicked(cameraImage: CameraImage, position: Int) {
        presenter?.imageLongClicked(cameraImage, position, imagesAdapter?.getSelectedCount() ?: 0)
    }

    override fun imageDeleteClicked(cameraImage: CameraImage, position: Int) {

    }

    override fun showImages(items: ArrayList<Any>) {
        imagesAdapter?.replaceItems(items)
    }

    override fun setSelectMode(selectMode: Boolean) {
        imagesAdapter?.setSelectMode(selectMode)
    }

    override fun itemChanged(position: Int) {
        imagesAdapter?.notifyItemChanged(position)
    }

    override fun clearSelectedImages() {
        imagesAdapter?.clearSelectedImages()
    }

    // Secure camera
    override fun goToSecureCamera(albumKey: String) {
        Intent(this, SecureCameraActivity::class.java)
                .putExtra(Constants.ALBUM_KEY, albumKey)
                .run { startActivity(this) }
    }

    // Image detail
    override fun goToImageDetail(albumKey: String, imageIndex: Int) {
        Intent(this, ImageDetailActivity::class.java)
                .putExtra(Constants.ALBUM_KEY, albumKey)
                .putExtra(Constants.IMAGE_INDEX, imageIndex)
                .run { startActivity(this) }
    }
}

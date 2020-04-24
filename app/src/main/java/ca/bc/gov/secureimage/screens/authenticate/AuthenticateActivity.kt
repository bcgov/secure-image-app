package ca.bc.gov.secureimage.screens.authenticate

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import ca.bc.gov.secureimage.R
import ca.bc.gov.secureimage.screens.albums.AlbumsActivity
import kotlinx.android.synthetic.main.activity_authenticate.*

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
class AuthenticateActivity : AppCompatActivity(), AuthenticateContract.View {

    override var presenter: AuthenticateContract.Presenter? = null

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticate)

        val keyguardManager: KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        AuthenticatePresenter(this, keyguardManager.isDeviceSecure, keyguardManager.isKeyguardSecure)
        presenter?.subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter?.onResult(
                resultCode == Activity.RESULT_OK,
                requestCode == CREDENTIALS_REQUEST_CODE)
    }

    // Messages
    override fun showNotSecureErrorMessage() {
        showToast(getString(R.string.device_not_secured))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // Logo
    override fun hideLogo() {
        logoIv.visibility = View.GONE
    }

    override fun showLogo() {
        logoIv.visibility = View.VISIBLE
    }

    // Info
    override fun hideInfo() {
        infoTv.visibility = View.GONE
    }

    override fun showInfo() {
        infoTv.visibility = View.VISIBLE
    }

    // Authenticate
    override fun hideAuthenticate() {
        authenticateTv.visibility = View.GONE
    }

    override fun showAuthenticate() {
        authenticateTv.visibility = View.VISIBLE
    }

    override fun setUpAuthenticateListener() {
        authenticateTv.setOnClickListener {
            presenter?.authenticateClicked()
        }
    }

    // Credentials
    override fun goToConfirmDeviceCredentials() {
        val keyguardManager: KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val credentialsIntent = keyguardManager.createConfirmDeviceCredentialIntent("", "")
        startActivityForResult(credentialsIntent, CREDENTIALS_REQUEST_CODE)
    }

    // Albums
    override fun goToAlbums() {
        val goToAlbumsIntent = Intent(this, AlbumsActivity::class.java)
        startActivity(goToAlbumsIntent)
    }

    companion object {
        const val CREDENTIALS_REQUEST_CODE = 2357
    }
}

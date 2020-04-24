package ca.bc.gov.secureimage.screens.authenticate

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
class AuthenticatePresenter(
        private val view: AuthenticateContract.View,
        private val isDeviceSecure: Boolean,
        private val isKeyguardSecure: Boolean
) : AuthenticateContract.Presenter {

    init {
        view.presenter = this
    }

    override fun subscribe() {
        if (!isDeviceSecure || !isKeyguardSecure) {
            view.showNotSecureErrorMessage()
            view.finish()

        } else {
            view.showLogo()
            view.showInfo()
            view.showAuthenticate()
            view.setUpAuthenticateListener()
        }
    }

    override fun dispose() {

    }

    // Authenticate
    override fun authenticateClicked() {
        view.goToConfirmDeviceCredentials()
    }

    // Result
    override fun onResult(isResultOk: Boolean, isCredentialsRequestCode: Boolean) {
        if (isResultOk && isCredentialsRequestCode) {
            view.goToAlbums()
        }
    }
}
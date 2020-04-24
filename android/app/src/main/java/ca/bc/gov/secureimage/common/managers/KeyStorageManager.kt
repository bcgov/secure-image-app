package ca.bc.gov.secureimage.common.managers

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

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
class KeyStorageManager(private val keyStore: KeyStore) {

    init {
        keyStore.load(null)
    }

    /**
     * Gets secure key using AES encryption
     */
    fun getSecureKey(
            alias: String,
            keySize: Int,
            sharedPrefs: SharedPreferences
    ): ByteArray {

        // Gets AES secret key from key store
        val aesSecretKey = getAESSecretKey(alias, 256)

        // Creates new random key and encrypts it using the AES secret key
        if(!sharedPrefs.contains(alias)) {
            generateRandomAESEncryptedKey(alias, keySize, aesSecretKey, sharedPrefs)
        }

        // Gets stored key and decrypts it using the AES secret key
        return getDecryptedKey(alias, aesSecretKey, sharedPrefs)
    }

    fun getAESSecretKey(
            alias: String,
            keySize: Int
    ): SecretKey {

        // Generating AES key if alias is not found
        if (!keyStore.containsAlias(alias)) {
            generateAESSecretKey(alias, keySize)
        }

        // Getting secret key from keystore and returning
        return keyStore.getKey(alias, null) as SecretKey
    }

    fun generateAESSecretKey(alias: String, keySize: Int) {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keyStore.provider)

        keyGenerator.init(KeyGenParameterSpec.Builder(
                alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(keySize)
                .build())

        keyGenerator.generateKey()
    }

    fun generateRandomAESEncryptedKey(
            alias: String,
            keySize: Int,
            aesSecretKey: SecretKey,
            sharedPrefs: SharedPreferences) {

        // Encryption setup
        val encryptionCipher = Cipher.getInstance(AES_TRANSFORMATION)
        encryptionCipher.init(Cipher.ENCRYPT_MODE, aesSecretKey)

        // New random encrypted key
        val randomKey = ByteArray(keySize)
        SecureRandom().nextBytes(randomKey)
        val encryptedBytes = encryptionCipher.doFinal(randomKey)

        // Saving base 64 encoded encrypted bytes
        val base64EncryptedBytesString = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        sharedPrefs.edit().putString(alias, base64EncryptedBytesString).apply()

        // Saving base 64 encoded initialization vector
        val base64IvString = Base64.encodeToString(encryptionCipher.iv, Base64.DEFAULT)
        sharedPrefs.edit().putString(getIvAlias(alias), base64IvString).apply()
    }

    fun getDecryptedKey(
            alias: String,
            aesSecretKey: SecretKey,
            sharedPrefs: SharedPreferences
    ): ByteArray {

        // Decoding saved encrypted bytes
        val base64EncryptedBytesString = sharedPrefs.getString(alias, "")
        val encryptedBytes = Base64.decode(base64EncryptedBytesString, Base64.DEFAULT)

        // Decoding saved initialization vector
        val base64IvString = sharedPrefs.getString(getIvAlias(alias), "")
        val savedIv = Base64.decode(base64IvString, Base64.DEFAULT)

        // Decryption setup
        val decryptionCipher = Cipher.getInstance(AES_TRANSFORMATION)
        val gcmParameterSpec = GCMParameterSpec(128, savedIv)
        decryptionCipher.init(Cipher.DECRYPT_MODE, aesSecretKey, gcmParameterSpec)

        // Decrypt encrypted bytes and return
        return decryptionCipher.doFinal(encryptedBytes)
    }

    private fun getIvAlias(alias: String): String {
        return "iv_key_$alias"
    }

    companion object {
        private val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    }

}
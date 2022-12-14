package com.p3t3rp3r3z.biometricconfigtester

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptographyManager {

data class EncryptedData(val ciphertext: ByteArray, val initializationVector: ByteArray)

    private val KEY_SIZE: Int = 256
    val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    private val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
    private val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES

    fun getInitializedCipherForEncryption(keyName: String): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        }catch (e: KeyPermanentlyInvalidatedException){
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.deleteEntry(keyName)
            getInitializedCipherForEncryption(keyName)
        }

        return cipher
    }

    fun getInitializedCipherForDecryption(keyName: String, initializationVector: ByteArray): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, initializationVector))
        return cipher
    }

    fun encryptData(plaintext: String, cipher: Cipher): EncryptedData {
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return EncryptedData(ciphertext,cipher.iv)
    }

    fun decryptData(ciphertext: ByteArray, cipher: Cipher): String {
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charset.forName("UTF-8"))
    }

    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    private fun getOrCreateSecretKey(keyName: String): SecretKey {
        // If Secretkey was previously created for that keyName, then grab and return it.
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        try {
            keyStore.getKey(keyName, null)?.let { return it as SecretKey }
        }catch (e:Exception){
            keyStore.deleteEntry(keyName)
            keyStore.getKey(keyName, null)?.let { return it as SecretKey }
        }


        // if you reach here, then a new SecretKey must be generated for that keyName
        val paramsBuilder = KeyGenParameterSpec.Builder(keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
            setUserAuthenticationRequired(true)
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE)
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

}
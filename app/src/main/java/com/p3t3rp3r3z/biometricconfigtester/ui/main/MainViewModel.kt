package com.p3t3rp3r3z.biometricconfigtester.ui.main

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.p3t3rp3r3z.biometricconfigtester.CryptographyManager
import javax.crypto.Cipher
import kotlin.concurrent.fixedRateTimer

//for testing biometrics
private const val secretKey = "biometrics_secret_key"
class MainViewModel : ViewModel() {
    private lateinit var ciphertext:ByteArray
    var initializationVector: ByteArray = ByteArray(12)
    val cryptographyManager = CryptographyManager()

    lateinit var biometricPrompt : BiometricPrompt

    fun createWeakPrompt (context: Context, activity: Fragment) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(
                    result: androidx.biometric.BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    result.cryptoObject
                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                }
            })


        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Prompt WEAK")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun createStrongPrompt (context: Context, activity: Fragment) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(
                    result: androidx.biometric.BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    result.cryptoObject
                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                }
            })


        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Prompt STRONG")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun createBiometricPrompt(context: Context, activity: Fragment, callback: BiometricPrompt.AuthenticationCallback): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(context)

        //The API requires the client/Activity context for displaying the prompt view
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        return biometricPrompt
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Prompt STRONG")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        return promptInfo
    }

    fun encryptValue(string:String, activity: Fragment) {
        val callback = object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(activity.requireContext(), "Error", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(
                result: androidx.biometric.BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)
                encryptData(string, result.cryptoObject)
                Toast.makeText(activity.requireContext(), "Successful encryption", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(activity.requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            }
        }

        biometricPrompt = createBiometricPrompt(activity.requireContext(),activity, callback)

        if (BiometricManager.from(activity.requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager
                .BIOMETRIC_SUCCESS && !string.isNullOrBlank()) {
            biometricPrompt.authenticate(createPromptInfo(),BiometricPrompt.CryptoObject(cryptographyManager.getInitializedCipherForEncryption(secretKey)))
        }
    }

    fun decryptValue(activity: Fragment, callback: BiometricPrompt.AuthenticationCallback) : String{
        var decryptedValue = "Failure"
        val cipher = try {

        cryptographyManager.getInitializedCipherForDecryption(secretKey, initializationVector)
        }catch (e:Exception){
            null
        }

        biometricPrompt = createBiometricPrompt(activity.requireContext(), activity,
            callback)
        if (BiometricManager.from(activity.requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager
                .BIOMETRIC_SUCCESS && cipher != null) {

            biometricPrompt.authenticate(createPromptInfo(), BiometricPrompt.CryptoObject(cipher))
            return decryptedValue
        }
        return decryptedValue
    }

    private fun encryptData( string: String, cryptoObject: BiometricPrompt.CryptoObject?) {
        val encryptedData = cryptographyManager.encryptData(string, cryptoObject?.cipher!!)
        initializationVector = encryptedData.initializationVector
        ciphertext = encryptedData.ciphertext

    }

    fun decryptData(cryptoObject: BiometricPrompt.CryptoObject?) : String?{
        return cryptographyManager.decryptData(ciphertext, cryptoObject?.cipher!!)
    }
}
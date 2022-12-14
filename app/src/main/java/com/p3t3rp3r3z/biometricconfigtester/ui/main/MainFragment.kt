package com.p3t3rp3r3z.biometricconfigtester.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.p3t3rp3r3z.biometricconfigtester.R

class MainFragment : Fragment() {
    private lateinit var weakButton : Button
    private lateinit var strongButton : Button
    private lateinit var encryptButton : Button
    private lateinit var decryptButton : Button
    private lateinit var editText: EditText
    private lateinit var output: TextView

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        weakButton = requireView().findViewById(R.id.button)
        strongButton = requireView().findViewById(R.id.button2)
        encryptButton = requireView().findViewById(R.id.button3)
        decryptButton = requireView().findViewById(R.id.button4)
        editText = requireView().findViewById(R.id.editTextText)
        output = requireView().findViewById(R.id.textView)
        val callback = object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()

            }

            override fun onAuthenticationSucceeded(
                result: androidx.biometric.BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)
                val resultString = viewModel.decryptData(result.cryptoObject)
                output.text = resultString ?: "Failure, nothing to decrypt"
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        weakButton.setOnClickListener { viewModel.createWeakPrompt(requireContext(), this!!) }

        strongButton.setOnClickListener { viewModel.createStrongPrompt(requireContext(), this!!) }

        encryptButton.setOnClickListener {
            var textToEncrypt = if (editText.text.toString().isNotBlank()){
                editText.text.toString()
            } else "Test Value"
            viewModel.encryptValue(textToEncrypt, this!!) }

        decryptButton.setOnClickListener { viewModel.decryptValue( this!!, callback) }
    }

}
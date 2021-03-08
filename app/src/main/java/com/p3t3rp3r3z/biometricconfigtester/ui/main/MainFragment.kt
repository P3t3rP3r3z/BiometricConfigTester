package com.p3t3rp3r3z.biometricconfigtester.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.p3t3rp3r3z.biometricconfigtester.R

class MainFragment : Fragment() {
    private lateinit var weakButton : Button
    private lateinit var strongButton : Button

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
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        weakButton.setOnClickListener { viewModel.createWeakPrompt(requireContext(), this!!) }

        strongButton.setOnClickListener { viewModel.createStrongPrompt(requireContext(), this!!) }
    }

}
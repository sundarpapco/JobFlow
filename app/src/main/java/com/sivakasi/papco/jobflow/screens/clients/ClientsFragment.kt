package com.sivakasi.papco.jobflow.screens.clients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sivakasi.papco.jobflow.databinding.ComposeScreenBinding
import com.sivakasi.papco.jobflow.screens.clients.ui.ClientsScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme

class ClientsFragment: Fragment() {

    private var _viewBinding:ComposeScreenBinding?=null
    private val viewBinding:ComposeScreenBinding=_viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding= ComposeScreenBinding.inflate(inflater,container,false)
        viewBinding.composeView.setContent {
            JobFlowTheme {
                ClientsScreen()
            }
        }
        return viewBinding.root
    }
}
package com.sivakasi.papco.jobflow.screens.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sivakasi.papco.jobflow.MainActivityVM
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class NoInternetFragment : Fragment() {

    @ExperimentalCoroutinesApi
    private val viewModel: MainActivityVM by lazy{
        ViewModelProvider(requireActivity()).get(MainActivityVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply{
            setContent {
                NoInternetScreen(
                    viewModel.isInitializing,
                    viewModel::initializeCurrentUser
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onStop() {
        super.onStop()
        showActionBar()
    }
}
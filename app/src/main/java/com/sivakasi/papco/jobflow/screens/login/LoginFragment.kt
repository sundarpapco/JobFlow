package com.sivakasi.papco.jobflow.screens.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(LoginFragmentVM::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                LoginScreen(
                    authState = viewModel.authState,
                    onFormSubmit = viewModel::onFormSubmit,
                    onForgotPassword = this@LoginFragment::navigateToForgotPasswordScreen,
                    onModeChange = viewModel::onModeChanged
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


    private fun navigateToForgotPasswordScreen() {
        findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }
}
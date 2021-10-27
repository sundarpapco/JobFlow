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
import androidx.navigation.NavOptions
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
                    onModeChange = viewModel::onModeChanged,
                    onConnectionTryAgain = viewModel::checkUserStatus
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //disableBackArrow()
        //updateFragmentTitle()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onStop() {
        super.onStop()
        showActionBar()
    }

    private fun observeViewModel() {
        viewModel.loginSuccess.observe(viewLifecycleOwner) { role ->
            saveUserRole(role)
            navigateByClaim(role)
        }
    }

    private fun navigateToForgotPasswordScreen() {
        findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }

    private fun navigateByClaim(claim: String) {
        when (claim) {
            "root" -> {
                navigateToHomeScreen()
            }

            "admin" -> {
                navigateToHomeScreen()
            }

            "printer" -> {
                navigateToMachinesScreen()
            }

            "guest" -> {
                navigateToGuestScreen()
            }
        }
    }

    private fun navigateToHomeScreen() {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
        findNavController().navigate(R.id.action_global_fragmentHome, null, navOptions)
    }

    private fun navigateToMachinesScreen() {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
        findNavController().navigate(
            R.id.action_global_manageMachinesFragment,
            null,
            navOptions
        )
    }

    private fun navigateToGuestScreen() {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
        findNavController().navigate(R.id.action_global_guestFragment, null, navOptions)
    }
}
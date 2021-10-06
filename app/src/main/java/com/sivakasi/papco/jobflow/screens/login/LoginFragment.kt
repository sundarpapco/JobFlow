package com.sivakasi.papco.jobflow.screens.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.databinding.ComposeScreenBinding
import com.sivakasi.papco.jobflow.extensions.disableBackArrow
import com.sivakasi.papco.jobflow.extensions.saveUserRole
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private var _viewBinding: ComposeScreenBinding? = null
    private val viewBinding: ComposeScreenBinding
        get() = _viewBinding!!

    private val viewModel by lazy {
        ViewModelProvider(this).get(LoginFragmentVM::class.java)
    }

    /* override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         if (auth.currentUser != null)
             onSuccessfulLogin()
         else
             saveUserRole("guest")

     }*/


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = ComposeScreenBinding.inflate(inflater, container, false)
        viewBinding.composeView.setContent {
            LoginScreen(
                authState = viewModel.authState,
                onFormSubmit = viewModel::onFormSubmit,
                onForgotPassword = this::navigateToForgotPasswordScreen,
                onModeChange = viewModel::onModeChanged
            )
        }
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disableBackArrow()
        updateFragmentTitle()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loginSuccess.observe(viewLifecycleOwner) { role ->
            saveUserRole(role)
            navigateByClaim(role)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun onSuccessfulLogin() {
        auth.currentUser?.getIdToken(false)
            ?.addOnSuccessListener {
                val claim = it.claims["role"] as String
                //save the current user role in the activity for future reference from other fragments
                //throughout the application life cycle
                saveUserRole(claim)
                navigateByClaim(claim)
            }
            ?.addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_unknown_error),
                    Toast.LENGTH_SHORT
                ).show()
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

    private fun updateFragmentTitle() {
        updateTitle(getString(R.string.papco_jobs))
        updateSubTitle("")
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
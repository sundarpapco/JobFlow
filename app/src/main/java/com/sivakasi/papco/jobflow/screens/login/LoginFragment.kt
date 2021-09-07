package com.sivakasi.papco.jobflow.screens.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.databinding.ComposeScreenBinding
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var currentUser=FirebaseAuth.getInstance().currentUser
    private var _viewBinding: ComposeScreenBinding? = null
    private val viewBinding: ComposeScreenBinding
        get() = _viewBinding!!

    private val viewModel by lazy {
        ViewModelProvider(this).get(LoginFragmentVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(currentUser!=null)
            onSuccessfulLogin()
    }

    @ExperimentalComposeUiApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = ComposeScreenBinding.inflate(inflater, container, false)
        viewBinding.composeView.setContent {
            JobFlowTheme {
                LoginScreen(
                    loginState = viewModel.loginState,
                    viewModel::onLogin,
                    onLoginSuccess = this::onSuccessfulLogin
                )
            }
        }
        return viewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding=null
    }

    private fun onSuccessfulLogin() {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
        findNavController().navigate(R.id.action_loginFragment_to_fragmentHome, null, navOptions)
    }
}
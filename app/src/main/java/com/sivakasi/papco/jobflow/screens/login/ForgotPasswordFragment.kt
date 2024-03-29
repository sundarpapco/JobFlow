package com.sivakasi.papco.jobflow.screens.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.enableBackArrow
import com.sivakasi.papco.jobflow.extensions.registerBackArrowMenu
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProvider(this)[ForgotPasswordVM::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                ForgotPasswordScreen(
                    state = viewModel.forgotPasswordState,
                    onFormSubmit = viewModel::onFormSubmit
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        updateFragmentTitle()
        enableBackArrow()
        registerBackArrowMenu()
    }

    private fun observeViewModel() {
        viewModel.passwordResetMailSent.observe(viewLifecycleOwner) { success ->
            if (success)
                findNavController().popBackStack()
        }

    }

    private fun updateFragmentTitle(){
        updateTitle(getString(R.string.reset_password))
        updateSubTitle("")
    }


}
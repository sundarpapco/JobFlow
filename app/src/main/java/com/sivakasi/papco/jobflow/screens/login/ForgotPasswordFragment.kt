package com.sivakasi.papco.jobflow.screens.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.databinding.ComposeScreenBinding
import com.sivakasi.papco.jobflow.extensions.enableBackArrow
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    private var _viewBinding: ComposeScreenBinding? = null
    private val viewBinding: ComposeScreenBinding
        get() = _viewBinding!!

    private val viewModel by lazy {
        ViewModelProvider(this).get(ForgotPasswordVM::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = ComposeScreenBinding.inflate(inflater, container, false)
        viewBinding.composeView.setContent {
            ForgotPasswordScreen(
                state = viewModel.forgotPasswordState,
                onFormSubmit = viewModel::onFormSubmit
            )
        }
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        updateFragmentTitle()
        enableBackArrow()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun observeViewModel() {
        viewModel.passwordResetMailSent.observe(viewLifecycleOwner) { success ->
            if (success)
                findNavController().popBackStack()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun updateFragmentTitle(){
        updateTitle(getString(R.string.reset_password))
        updateSubTitle("")
    }


}
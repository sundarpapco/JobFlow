package com.sivakasi.papco.jobflow.admin

import android.os.Bundle
import android.view.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.databinding.ComposeScreenBinding
import com.sivakasi.papco.jobflow.extensions.enableBackArrow
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.screens.login.LoginFragmentVM
import com.sivakasi.papco.jobflow.screens.login.LoginScreen
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalComposeUiApi
@AndroidEntryPoint
class UpdateRoleFragment: Fragment() {

    private var _viewBinding: ComposeScreenBinding? = null
    private val viewBinding: ComposeScreenBinding
        get() = _viewBinding!!

    private val viewModel by lazy {
        ViewModelProvider(this).get(UpdateRoleVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = ComposeScreenBinding.inflate(inflater, container, false)
        viewBinding.composeView.setContent {
            UpdateRoleScreen(updateRoleState = viewModel.state,viewModel::onUpdateRole)
        }
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableBackArrow()
        updateFragmentTitle()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding=null
    }

    private fun updateFragmentTitle(){
        updateTitle(getString(R.string.update_role))
        updateSubTitle("")
    }
}
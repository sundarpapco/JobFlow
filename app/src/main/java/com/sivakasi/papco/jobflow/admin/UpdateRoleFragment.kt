package com.sivakasi.papco.jobflow.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.enableBackArrow
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
@AndroidEntryPoint
class UpdateRoleFragment : Fragment() {


    private val viewModel: UpdateRoleVM by hiltNavGraphViewModels(R.id.update_role_flow)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                UpdateRoleScreen(
                    updateRoleState = viewModel.state,
                    onSubmit = viewModel::onUpdateRole,
                    onUserChange = {
                        //Navigate to the select User screen
                        findNavController().navigate(R.id.action_updateRoleFragment_to_selectUserFragment)
                    }
                )
            }
        }
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

    private fun updateFragmentTitle() {
        updateTitle(getString(R.string.update_role))
        updateSubTitle("")
    }
}
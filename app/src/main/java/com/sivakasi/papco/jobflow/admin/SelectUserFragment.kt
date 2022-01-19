package com.sivakasi.papco.jobflow.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class SelectUserFragment : Fragment() {

    private val viewModel: UpdateRoleVM by hiltNavGraphViewModels(R.id.update_role_flow)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                JobFlowTheme{
                    UsersListScreen(
                        users = viewModel.users,
                        onClick = {
                            viewModel.selectUser(it)
                            findNavController().popBackStack()
                        },
                        onBackPressed = { findNavController().popBackStack() }
                    )
                }

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
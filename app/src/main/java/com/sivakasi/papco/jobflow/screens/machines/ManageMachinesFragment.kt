@file:Suppress("UNCHECKED_CAST")

package com.sivakasi.papco.jobflow.screens.machines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.databinding.ComposeScreenBinding
import com.sivakasi.papco.jobflow.extensions.currentUserRole
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.extensions.signOut
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ManageMachinesFragment : Fragment() {

    companion object {
        private const val KEY_SELECT_MODE = "key:selection:mode"
        const val KEY_SELECTED_MACHINE_ID = "key:selected:machine:id"

        fun getArguments(isSelectionMode: Boolean): Bundle {
            return Bundle().apply {
                putBoolean(KEY_SELECT_MODE, isSelectionMode)
            }
        }
    }

    private var _viewBinding: ComposeScreenBinding? = null
    private val viewBinding: ComposeScreenBinding
        get() = _viewBinding!!

    private val viewModel: ManageMachinesVM by lazy {
        ViewModelProvider(this).get(ManageMachinesVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.uiState.apply {
            role=currentUserRole()
            selectionMode=isSelectionMode()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = ComposeScreenBinding.inflate(inflater,container,false)

        viewBinding.composeView.setContent {
            ManageMachinesScreen(
                navController = findNavController(),
                onSignOut = this::onSignOut,
                viewModel = viewModel
            )
        }

        return viewBinding.root
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onStop() {
        super.onStop()
        showActionBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun isSelectionMode(): Boolean =
        arguments?.getBoolean(KEY_SELECT_MODE) ?: false

    private fun onSignOut() = signOut()
}
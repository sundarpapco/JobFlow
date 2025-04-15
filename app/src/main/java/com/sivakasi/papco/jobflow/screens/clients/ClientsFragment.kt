package com.sivakasi.papco.jobflow.screens.clients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.screens.clients.ui.ClientsScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalFoundationApi
@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ClientsFragment : Fragment() {

    companion object {

        const val KEY_CLIENT = "key:client:bundle"
        private const val KEY_SELECTION_MODE = "key:selection:mode"

        fun getArguments(isSelectionMode: Boolean = false): Bundle {
            return Bundle().apply {
                putBoolean(KEY_SELECTION_MODE, isSelectionMode)
            }
        }
    }

    private val viewModel: ClientsFragmentVM by lazy {
        ViewModelProvider(this)[ClientsFragmentVM::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply{
            setContent {
                JobFlowTheme {
                    ClientsScreen(
                        screenState = viewModel.screenState,
                        isSelectionMode = isSelectionMode(),
                        onClientSelected = {id,name->
                            selectClientAndClose(Client(id,name))
                        },
                        onClientEdit = {_,name->
                            viewModel.onUpdateClient(name)
                        },
                        onClientAdd = {
                            viewModel.onAddClient(it)
                        },
                        onBackPressed = {findNavController().popBackStack()}
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

    private fun selectClientAndClose(selectedClient: Client) {
        val controller = findNavController()
        controller.previousBackStackEntry?.savedStateHandle?.set(
            KEY_CLIENT,
            selectedClient
        )
        controller.popBackStack()
    }

    private fun isSelectionMode(): Boolean =
        arguments?.getBoolean(KEY_SELECTION_MODE, false) ?: false
}
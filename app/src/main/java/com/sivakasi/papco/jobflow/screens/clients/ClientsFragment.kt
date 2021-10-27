package com.sivakasi.papco.jobflow.screens.clients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.extensions.enableBackArrow
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.screens.clients.ui.ClientsScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.util.LoadingStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

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
        ViewModelProvider(this).get(ClientsFragmentVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply{
            setContent {
                JobFlowTheme {
                    ClientsScreen(viewModel, isSelectionMode())
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableBackArrow()
        updateTitle(if (isSelectionMode()) getString(R.string.select_client) else getString(R.string.clients))
        updateSubTitle("")
        observeViewModel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    @Suppress("UNCHECKED_CAST")
    private fun observeViewModel() {
        viewModel.clientsList.observe(viewLifecycleOwner) { status ->
            if (status is LoadingStatus.Success<*>) {
                val list = (status.data as List<Client>)
                if (list.isNotEmpty())
                    updateSubTitle(getString(R.string.xx_clients, list.size))
                else
                    updateSubTitle("")
            }
        }

        viewModel.selectedClient.observe(viewLifecycleOwner) {
            selectClientAndClose(it)
        }
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
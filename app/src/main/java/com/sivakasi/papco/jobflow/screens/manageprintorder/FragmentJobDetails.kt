package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.screens.clients.ClientsFragment
import com.sivakasi.papco.jobflow.screens.manageprintorder.jobDetails.JobDetailsScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FragmentJobDetails : Fragment() {

    private val viewModel: ManagePrintOrderVM by hiltNavGraphViewModels(R.id.print_order_flow)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                JobFlowTheme {
                    JobDetailsScreen(
                        screenState = viewModel.jobDetailsScreenState,
                        onSelectClientName = { navigateToClientSelectionScreen() },
                        onNext = { navigateToNextScreen() },
                        onClosePressed = { exitOutOfCreationFlow() }
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


    @OptIn(ExperimentalFoundationApi::class)
    private fun observeViewModel() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.recoveringFromProcessDeath.collect {
                        if (it)
                            exitOutOfCreationFlow()
                    }
                }

                launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED){
                        val handle = findNavController().currentBackStackEntry?.savedStateHandle
                        handle?.getStateFlow<Client?>(ClientsFragment.KEY_CLIENT,null)?.
                        collect{
                            it?.let {selectedClient->
                                viewModel.jobDetailsScreenState
                                    .selectClient(selectedClient.id,selectedClient.name)
                                handle[ClientsFragment.KEY_CLIENT]=null
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun navigateToClientSelectionScreen() {
        findNavController().navigate(
            R.id.action_fragmentJobDetails_to_clientSelectionFragment,
            ClientsFragment.getArguments(true)
        )
    }

    private fun exitOutOfCreationFlow() {
        findNavController().popBackStack(R.id.fragmentJobDetails, true)
    }

    private fun navigateToNextScreen() =
        findNavController().navigate(R.id.action_fragmentJobDetails_to_fragmentPaperDetails)


}
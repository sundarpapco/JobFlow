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
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.clearErrorOnTextChange
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.databinding.FragmentJobDetailsBinding
import com.sivakasi.papco.jobflow.extensions.*
import com.sivakasi.papco.jobflow.screens.clients.ClientsFragment
import com.sivakasi.papco.jobflow.screens.manageprintorder.jobDetails.JobDetailsScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.util.FormValidator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FragmentJobDetails : Fragment() {

    private val viewModel: ManagePrintOrderVM by hiltNavGraphViewModels(R.id.print_order_flow)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
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

        viewModel.recoveringFromProcessDeath.observe(viewLifecycleOwner){
            if(it)
                exitOutOfCreationFlow()
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Client>(
            ClientsFragment.KEY_CLIENT
        )?.observe(viewLifecycleOwner) {

            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Client>(
                ClientsFragment.KEY_CLIENT
            )

            viewModel.jobDetailsScreenState.selectClient(it.id,it.name)
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
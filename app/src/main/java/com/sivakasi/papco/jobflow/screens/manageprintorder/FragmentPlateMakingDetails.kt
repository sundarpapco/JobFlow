package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.screens.manageprintorder.plateMakingDetails.PlateMakingScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FragmentPlateMakingDetails : Fragment() {

    private val viewModel: ManagePrintOrderVM by hiltNavGraphViewModels(R.id.print_order_flow)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                JobFlowTheme {
                    PlateMakingScreen(
                      screenState = viewModel.plateMakingDetailsScreenState,
                        onNext = {
                            findNavController()
                                .navigate(R.id.action_fragmentPlateMakingDetails_to_fragmentPrintingDetail)
                        },
                        onClose = {exitOutOfCreationFlow()}
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

    private fun observeViewModel() {
        viewModel.recoveringFromProcessDeath.observe(viewLifecycleOwner){
            if(it)
                exitOutOfCreationFlow()
        }
    }

    private fun exitOutOfCreationFlow() {
        findNavController().popBackStack(R.id.fragmentJobDetails, true)
    }
}
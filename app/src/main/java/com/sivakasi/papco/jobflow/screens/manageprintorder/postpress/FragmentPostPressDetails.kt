package com.sivakasi.papco.jobflow.screens.manageprintorder.postpress

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
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.hideKeyboard
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.extensions.toastError
import com.sivakasi.papco.jobflow.screens.manageprintorder.ManagePrintOrderVM
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.util.LoadingStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FragmentPostPressDetails : Fragment() {


    private val viewModel: ManagePrintOrderVM by hiltNavGraphViewModels(R.id.print_order_flow)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                JobFlowTheme {
                    PostPressScreen(
                        state = viewModel.postPressScreenState,
                        onSavePrintOrder = { viewModel.savePrintOrder() },
                        onUpdatePrintOrder = { viewModel.updatePrintOrder() },
                        onClose = { exitOutOfCreationFlow() }
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
        viewModel.recoveringFromProcessDeath.observe(viewLifecycleOwner) {
            if (it)
                exitOutOfCreationFlow()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveUpdateStatus.collect {
                    it?.let {
                        if (!it.isAlreadyHandled())
                            handleSaveUpdateEvent(it.handleEvent())
                    }
                }
            }
        }
    }

    private fun handleSaveUpdateEvent(status: LoadingStatus) {
        when(status){
            is LoadingStatus.Success<*>->{
                exitOutOfCreationFlow()
            }

            is LoadingStatus.Error->{
                requireContext().toastError(status.exception)
            }

            else->{

            }
        }
    }


    private fun exitOutOfCreationFlow() {
        findNavController().popBackStack(R.id.fragmentJobDetails, true)
    }

}
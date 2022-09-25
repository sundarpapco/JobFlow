@file:Suppress("UNCHECKED_CAST")

package com.sivakasi.papco.jobflow.screens.invoicehistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.enableBackArrow
import com.sivakasi.papco.jobflow.extensions.registerBackArrowMenu
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.viewprintorder.ComposeViewPrintOrderFragment
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class InvoiceHistoryFragment : Fragment() {


    private val viewModel: InvoiceHistoryVM by lazy {
        ViewModelProvider(this).get(InvoiceHistoryVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableBackArrow()
    }

    @ExperimentalComposeUiApi
    @FlowPreview
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                JobFlowTheme {
                    InvoiceHistoryScreen(
                        viewModel = viewModel,
                        onItemClicked = this@InvoiceHistoryFragment::onItemClick
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTitle(getString(R.string.invoice_history))
        updateSubTitle("")
        registerBackArrowMenu()
    }

    //Navigate to view Print order screen
    @ExperimentalComposeUiApi
    @FlowPreview
    private fun onItemClick(item: SearchModel) {

        viewModel.observePrintOrder(item)

        val arguments = ComposeViewPrintOrderFragment.getArguments(item.printOrderNumber)

        findNavController().navigate(
            R.id.action_invoiceHistoryFragment_to_composeViewPrintOrderFragment,
            arguments
        )
    }

}
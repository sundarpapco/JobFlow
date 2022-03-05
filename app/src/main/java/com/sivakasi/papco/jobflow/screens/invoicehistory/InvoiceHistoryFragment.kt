@file:Suppress("UNCHECKED_CAST")

package com.sivakasi.papco.jobflow.screens.invoicehistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.extensions.enableBackArrow
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.viewprintorder.ViewPrintOrderFragment
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

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

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    //Navigate to view Print order screen
    private fun onItemClick(item: SearchModel) {

        viewModel.observePrintOrder(item)

        val arguments = ViewPrintOrderFragment.getArguments(
            item.destinationId,
            PrintOrder.documentId(item.printOrderNumber)
        )

        findNavController().navigate(
            R.id.action_invoiceHistoryFragment_to_viewPrintOrderFragment,
            arguments
        )
    }

}
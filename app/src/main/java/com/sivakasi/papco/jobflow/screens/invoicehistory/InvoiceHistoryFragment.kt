@file:Suppress("UNCHECKED_CAST")

package com.sivakasi.papco.jobflow.screens.invoicehistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.databinding.DestinationFixedBinding
import com.sivakasi.papco.jobflow.extensions.enableBackArrow
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.search.SearchAdapter
import com.sivakasi.papco.jobflow.screens.search.SearchAdapterListener
import com.sivakasi.papco.jobflow.screens.viewprintorder.ViewPrintOrderFragment
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class InvoiceHistoryFragment : Fragment(), SearchAdapterListener {

    private var _viewBinding: DestinationFixedBinding? = null
    private val viewBinding: DestinationFixedBinding
        get() = _viewBinding!!

    private val adapter: SearchAdapter by lazy {
        SearchAdapter(this)
    }

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
        _viewBinding = DestinationFixedBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTitle(getString(R.string.invoice_history))
        updateSubTitle("")
        initViews()
        observeViewModel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.recycler.adapter = null
        _viewBinding = null
    }

    private fun initViews() {
        viewBinding.lblLastCompletionTime.visibility = View.GONE
        viewBinding.fab.hide()
        initRecycler()
    }

    private fun initRecycler() {
        viewBinding.recycler.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.recycler.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.invoiceHistory.observe(viewLifecycleOwner) {
            handleLoadingStatus(it)
        }
    }

    //Navigate to view Print order screen
    override fun onItemClick(item: SearchModel) {
        val arguments = ViewPrintOrderFragment.getArguments(
            item.destinationId,
            PrintOrder.documentId(item.printOrderNumber)
        )

        findNavController().navigate(
            R.id.action_invoiceHistoryFragment_to_viewPrintOrderFragment,
            arguments
        )
    }

    private fun handleLoadingStatus(status: LoadingStatus) {

        when (status) {

            is LoadingStatus.Loading -> {
                renderLoadingState()
            }

            is LoadingStatus.Success<*> -> {
                renderDataState(status.data as List<SearchModel>)
            }

            is LoadingStatus.Error -> {
                toast(status.exception.message ?: getString(R.string.error_unknown_error))
                findNavController().popBackStack()
            }

        }

    }

    private fun renderLoadingState() {
        viewBinding.progressBar.root.visibility = View.VISIBLE
    }

    private fun renderDataState(data: List<SearchModel>) {
        viewBinding.progressBar.root.visibility = View.GONE
        adapter.submitList(data)
    }

}
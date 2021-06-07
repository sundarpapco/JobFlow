@file:Suppress("UNCHECKED_CAST")

package com.sivakasi.papco.jobflow.screens.search

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.databinding.FragmentSearchBinding
import com.sivakasi.papco.jobflow.extensions.enableBackArrow
import com.sivakasi.papco.jobflow.extensions.hideKeyboard
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.viewprintorder.ViewPrintOrderFragment
import com.sivakasi.papco.jobflow.util.LoadingStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SearchFragment : Fragment(), SearchAdapterListener {

    private var _viewBinding: FragmentSearchBinding? = null
    private val viewBinding: FragmentSearchBinding
        get() = _viewBinding!!

    private val viewModel: SearchVM by lazy {
        ViewModelProvider(this).get(SearchVM::class.java)
    }

    private val adapter: SearchAdapter by lazy {
        SearchAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentSearchBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTitle(getString(R.string.search))
        updateSubTitle("")
        enableBackArrow()
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

        viewBinding.txtSearch.filters = arrayOf(InputFilter.LengthFilter(9))

        viewBinding.txtSearch.setOnEditorActionListener { _, actionId, _ ->
            if(actionId==EditorInfo.IME_ACTION_SEARCH) {
                search()
                true
            }else
                false
        }

        viewBinding.btnSearch.setOnClickListener {
            search()
        }

        initRecycler()
    }

    private fun observeViewModel() {
        viewModel.searchStatus.observe(viewLifecycleOwner) {
            handleSearchStatus(it)
        }
    }

    private fun initRecycler() {
        viewBinding.recycler.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.recycler.adapter = adapter
    }

    private fun search(){
        hideKeyboard(requireContext(), viewBinding.txtSearch)
        viewModel.search(viewBinding.txtSearch.text.toString().trim())
    }

    private fun handleSearchStatus(status: LoadingStatus) {

        when (status) {

            is LoadingStatus.Loading -> {
                showLoadingState()
            }

            is LoadingStatus.Success<*> -> {
                val data = status.data as List<SearchModel>
                if (data.isEmpty())
                    showErrorState(getString(R.string.no_results_found))
                else
                    showResultState(status.data)
            }

            is LoadingStatus.Error -> {
                showErrorState(status.exception.message ?: getString(R.string.error_unknown_error))
            }
        }

    }

    override fun onItemClick(item: SearchModel) {
        navigateToViewPrintOrderScreen(item)
    }

    private fun navigateToViewPrintOrderScreen(item:SearchModel){
        val args=ViewPrintOrderFragment.getArguments(
            item.destinationId,
            PrintOrder.documentId(item.printOrderNumber)
        )
        findNavController().navigate(R.id.action_searchFragment_to_viewPrintOrderFragment,args)
    }

    private fun showLoadingState() {
        viewBinding.txtError.visibility = View.GONE
        viewBinding.progressLayout.root.visibility = View.VISIBLE
        viewBinding.recycler.visibility = View.GONE
    }

    private fun showResultState(result: List<SearchModel>) {
        adapter.submitList(result)
        viewBinding.txtError.visibility = View.GONE
        viewBinding.progressLayout.root.visibility = View.GONE
        viewBinding.recycler.visibility = View.VISIBLE
    }

    private fun showErrorState(msg: String) {
        viewBinding.txtError.visibility = View.VISIBLE
        viewBinding.recycler.visibility = View.GONE
        viewBinding.progressLayout.root.visibility = View.GONE
        viewBinding.txtError.text = msg
    }
}
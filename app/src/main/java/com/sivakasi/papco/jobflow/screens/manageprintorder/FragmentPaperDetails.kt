package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PaperDetail
import com.sivakasi.papco.jobflow.databinding.FragmentPaperDetailsBinding
import com.sivakasi.papco.jobflow.extensions.enableBackAsClose
import com.sivakasi.papco.jobflow.extensions.toast
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FragmentPaperDetails : Fragment(),
    AddPaperDetailAdapter.CallBack,
    DialogPaperDetail.DialogPaperDetailListener,
    PaperDetailsAdapter.PaperDetailAdapterListener {

    private var _viewBinding: FragmentPaperDetailsBinding? = null
    private val viewBinding: FragmentPaperDetailsBinding
        get() = _viewBinding!!

    private val paperDetailAdapter: PaperDetailsAdapter by lazy {
        PaperDetailsAdapter(this)
    }

    private val adapter: ConcatAdapter by lazy {
        ConcatAdapter(paperDetailAdapter, AddPaperDetailAdapter(this))
    }

    private val viewModel: ManagePrintOrderVM by hiltNavGraphViewModels(R.id.print_order_flow)
    private var paperDetailCount =
        0 //Variable to hold number of paper details added to check for validation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentPaperDetailsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableBackAsClose()
        initViews()
        observeViewModel()

        if (viewModel.isEditMode)
            updateTitle(getString(R.string.edit_job))
        else
            updateTitle(getString(R.string.create_job))
        updateSubTitle("")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==android.R.id.home){
            findNavController().popBackStack(R.id.fragmentJobDetails, true)
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

        viewBinding.recycler.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.recycler.adapter = adapter

        viewBinding.btnNext.setOnClickListener {
            if (validateForm())
                findNavController().navigate(R.id.action_fragmentPaperDetails_to_fragmentPlateMakingDetails)
        }
    }

    private fun observeViewModel() {

        viewModel.recoveringFromProcessDeath.observe(viewLifecycleOwner){
            if(it)
                exitOutOfCreationFlow()
        }

        viewModel.loadedJob.observe(viewLifecycleOwner) {
                paperDetailCount = it.paperDetails?.let { list ->
                    paperDetailAdapter.submitList(list)
                    list.size
                } ?: 0
        }

    }

    override fun onAddPaperDetail() {
        showAddPaperDetailDialog()
    }

    override fun onSubmitPaperDetail(editIndex: Int, paperDetail: PaperDetail) {
        if (editIndex >= 0)
            viewModel.updatePaperDetail(editIndex, paperDetail)
        else
            viewModel.addPaperDetail(paperDetail)
    }

    override fun onEditPaperDetail(index: Int, paperDetail: PaperDetail) {
        showAddPaperDetailDialog(index, paperDetail)
    }

    override fun onDeletePaperDetail(index: Int) {
        viewModel.removePaperDetail(index)
    }

    private fun showAddPaperDetailDialog(
        editIndex: Int = -1,
        paperDetailToEdit: PaperDetail? = null
    ) {
        DialogPaperDetail.getInstance(editIndex, paperDetailToEdit).show(
            childFragmentManager,
            DialogPaperDetail.TAG
        )
    }

    private fun validateForm(): Boolean {
        return if (paperDetailCount > 0) {
            true
        } else {
            toast(getString(R.string.error_at_least_one_paper_detail_required))
            false
        }
    }

    private fun exitOutOfCreationFlow() {
        findNavController().popBackStack(R.id.fragmentJobDetails, true)
    }
}
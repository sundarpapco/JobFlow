package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.asDateString
import com.sivakasi.papco.jobflow.calendarWithTime
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.databinding.FragmentViewPrintOrderBinding
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ViewPrintOrderFragment : Fragment() {

    private var _viewBinding: FragmentViewPrintOrderBinding? = null
    private val viewBinding: FragmentViewPrintOrderBinding
        get() = _viewBinding!!
    private val viewModel by lazy {
        ViewModelProvider(this).get(ViewPrintOrderVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _viewBinding = FragmentViewPrintOrderBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun observerViewModel() {
        viewModel.loadedPrintOrder.observe(viewLifecycleOwner) {
            handleLoadingStatus(it)
        }
    }

    private fun renderPrintOrder(printOrder:PrintOrder){

        with(viewBinding){
            poDetails.txtDate.text=calendarWithTime(printOrder.creationTime).asDateString()
            poDetails.txtClientName.text=printOrder.billingName
            poDetails.txtJobName.text=printOrder.jobName
            val paperDetail=printOrder.printingSizePaperDetail()
            poDetails.txtPrintingQuantity.text=getString(R.string.xx_sheets,paperDetail.sheets)
        }

    }

    private fun handleLoadingStatus(loadingState: LoadingStatus) {

        when (loadingState) {

            is LoadingStatus.Loading -> {

            }

            is LoadingStatus.Success<*> -> {
                val printOrder = loadingState.data as PrintOrder
                renderPrintOrder(printOrder)
            }

            is LoadingStatus.Error -> {
                toast(loadingState.exception.message ?: getString(R.string.error_unknown_error))
            }

        }

    }
}
package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.clearErrorOnTextChange
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.databinding.FragmentJobDetailsBinding
import com.sivakasi.papco.jobflow.util.FormValidator
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.validateForNonBlank
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class FragmentJobDetails : Fragment() {

    private var _viewBinding: FragmentJobDetailsBinding? = null
    private val viewBinding: FragmentJobDetailsBinding
        get() = _viewBinding!!

    private val viewModel: ManagePrintOrderVM by navGraphViewModels(R.id.print_order_flow)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding= FragmentJobDetailsBinding.inflate(inflater,container,false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeViewModel()
    }

    override fun onPause() {
        super.onPause()
        saveJobDetails()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding=null
    }

    private fun initViews(){
        viewBinding.btnNext.setOnClickListener {
            if(validateForm()) {
                saveJobDetails()
                navigateToNextScreen()
            }
        }

        viewBinding.txtClientName.clearErrorOnTextChange()
        viewBinding.txtJobName.clearErrorOnTextChange()
    }

    private fun observeViewModel(){
        viewModel.loadedJob.observe(viewLifecycleOwner){
            if(it is LoadingStatus.Success<*>)
                renderJobDetails(it.data as PrintOrder)
        }
    }

    private fun renderJobDetails(printOrder: PrintOrder){
        viewBinding.txtJobName.setText(printOrder.jobName)
        viewBinding.txtClientName.setText(printOrder.billingName)
    }

    private fun validateForm():Boolean{

        val errorMsg=getString(R.string.required_field)
        val validator=FormValidator()
            .validate(viewBinding.txtClientName.validateForNonBlank(errorMsg))
            .validate(viewBinding.txtJobName.validateForNonBlank(errorMsg))

        return validator.isValid()

    }


    private fun saveJobDetails(){
        viewModel.saveJobDetails(
            viewBinding.txtJobName.text.toString(),
            viewBinding.txtClientName.text.toString()
        )
    }

    private fun navigateToNextScreen() =
        findNavController().navigate(R.id.action_fragmentJobDetails_to_fragmentPaperDetails)


}
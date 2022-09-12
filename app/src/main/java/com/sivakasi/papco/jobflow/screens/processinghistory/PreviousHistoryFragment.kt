package com.sivakasi.papco.jobflow.screens.processinghistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
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
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class PreviousHistoryFragment:Fragment() {

    companion object{
        private const val KEY_PLATE_NUMBER = "key:plate_number"

        fun getArgumentBundle(plateNumber:Int):Bundle{

            return Bundle().apply {
                putInt(KEY_PLATE_NUMBER,plateNumber)
            }

        }
    }

    private val viewModel: PreviousProcessingHistoryVM by lazy {
        ViewModelProvider(this).get(PreviousProcessingHistoryVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel.loadPreviousHistoryOfPlateNumber(getPlateNumber())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply{
            setContent {
                JobFlowTheme {
                    PreviousHistoryScreen(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableBackArrow()
        updateTitle(getString(R.string.previous_processing_history))
        updateSubTitle("")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return if(item.itemId == android.R.id.home){
            findNavController().popBackStack()
            true
        }else
            super.onOptionsItemSelected(item)
    }

    private fun getPlateNumber():Int =
        arguments?.getInt(KEY_PLATE_NUMBER) ?: error("Plate number argument not found")
}
package com.sivakasi.papco.jobflow.screens.viewprintorder

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
import com.sivakasi.papco.jobflow.extensions.currentUserRole
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.print.PrintOrderReport
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class ComposeViewPrintOrderFragment : Fragment() {

    companion object {
        private const val KEY_PO_NUMBER = "key:printOrder:number"

        fun getArguments(poNumber: Int) =
            Bundle().apply {
                putInt(KEY_PO_NUMBER, poNumber)
            }
    }

    @Inject
    lateinit var printOrderReport: PrintOrderReport
    private val viewModel by lazy {
        ViewModelProvider(this).get(ComposeViewModelFragmentVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadPrintOrder(getPoNumber(),currentUserRole())
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onStop() {
        super.onStop()
        showActionBar()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                ViewPrintOrderScreen(
                    viewModel =viewModel,
                    navController = findNavController(),
                    activityContext = requireContext()
                )
            }


        }
    }

    /*fun print() {
        viewModel.screenState.printOrder?.let{
            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .build()
            val printManager = requireContext().getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "PrintOrder"
            val printAdapter = PrintOrderAdapter(it, printOrderReport)
            printManager.print(jobName, printAdapter, printAttributes)
        }
    }*/

    private fun getPoNumber(): Int =
        arguments?.getInt(KEY_PO_NUMBER) ?: error("PO Number argument not found")
}
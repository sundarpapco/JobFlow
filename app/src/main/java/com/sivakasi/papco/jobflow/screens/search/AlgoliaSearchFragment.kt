package com.sivakasi.papco.jobflow.screens.search

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
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.viewprintorder.ComposeViewPrintOrderFragment
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class AlgoliaSearchFragment:Fragment() {

    private val viewModel: AlgoliaSearchVM by lazy {
        ViewModelProvider(this)[AlgoliaSearchVM::class.java]
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
                    AlgoliaSearchScreen(
                        viewModel = viewModel,
                        onItemClicked = this@AlgoliaSearchFragment::onItemClick,
                        onBackPressed = this@AlgoliaSearchFragment::onBackPressed
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onStop() {
        super.onStop()
        showActionBar()
    }


    //Navigate to view Print order screen
    @ExperimentalCoroutinesApi
    @ExperimentalComposeUiApi
    @FlowPreview
    private fun onItemClick(item: SearchModel) {

        viewModel.observePrintOrder(item)

        val arguments = ComposeViewPrintOrderFragment.getArguments(item.printOrderNumber)

        findNavController().navigate(
            R.id.action_algoliaSearchFragment_to_composeViewPrintOrderFragment,
            arguments
        )
    }

    private fun onBackPressed(){
        findNavController().popBackStack()
    }
}
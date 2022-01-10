package com.sivakasi.papco.jobflow.screens.search

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
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.extensions.*
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.viewprintorder.ViewPrintOrderFragment
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class AlgoliaSearchFragment:Fragment() {

    private val viewModel: AlgoliaSearchVM by lazy {
        ViewModelProvider(this).get(AlgoliaSearchVM::class.java)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    //Navigate to view Print order screen
    @ExperimentalCoroutinesApi
    private fun onItemClick(item: SearchModel) {
        val arguments = ViewPrintOrderFragment.getArguments(
            item.destinationId,
            PrintOrder.documentId(item.printOrderNumber)
        )

        findNavController().navigate(
            R.id.action_algoliaSearchFragment_to_viewPrintOrderFragment,
            arguments
        )
    }

    private fun onBackPressed(){
        findNavController().popBackStack()
    }
}
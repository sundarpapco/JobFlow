package com.sivakasi.papco.jobflow.screens.clients.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.data.PrintOrder
import com.sivakasi.papco.jobflow.databinding.ComposeScreenBinding
import com.sivakasi.papco.jobflow.extensions.enableBackArrow
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.clients.history.ui.ClientHistoryScreen
import com.sivakasi.papco.jobflow.screens.viewprintorder.ViewPrintOrderFragment
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import com.sivakasi.papco.jobflow.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ClientHistoryFragment : Fragment() {

    companion object {
        const val KEY_CLIENT = "key:client"

        fun getArgumentBundle(client: Client): Bundle {
            return Bundle().apply {
                putParcelable(KEY_CLIENT, client)
            }
        }
    }

    private var _viewBinding: ComposeScreenBinding? = null
    private val viewBinding: ComposeScreenBinding
        get() = _viewBinding!!

    private val client: Client by lazy {
        arguments?.getParcelable<Client>(KEY_CLIENT)
            ?: error("ClientHistoryFragment should be launched with a client argument")
    }

    private val viewModel: ClientHistoryVM by lazy {
        ViewModelProvider(this).get(ClientHistoryVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = ComposeScreenBinding.inflate(inflater, container, false)
        viewBinding.composeView.setContent {
            JobFlowTheme {
                ClientHistoryScreen(viewModel)
            }
        }
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadClientHistory(client.id)

        enableBackArrow()
        updateTitle(client.name)
        updateSubTitle(getString(R.string.client_history))
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding=null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun observeViewModel(){
        viewModel.clickedResult.observe(viewLifecycleOwner,EventObserver{
            navigateToViewPrintOrderScreen(it)
        })
    }


    private fun navigateToViewPrintOrderScreen(searchModel:SearchModel){
        val args= ViewPrintOrderFragment.getArguments(
            searchModel.destinationId,
            PrintOrder.documentId(searchModel.printOrderNumber)
        )
        findNavController().navigate(R.id.action_clientHistoryFragment_to_viewPrintOrderFragment,args)
    }
}
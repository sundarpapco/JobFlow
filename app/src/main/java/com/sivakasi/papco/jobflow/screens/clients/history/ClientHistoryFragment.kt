package com.sivakasi.papco.jobflow.screens.clients.history

import android.os.Build
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
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.extensions.enableBackArrow
import com.sivakasi.papco.jobflow.extensions.registerBackArrowMenu
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.screens.viewprintorder.ComposeViewPrintOrderFragment
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

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

    @Suppress("DEPRECATION")
    private val client: Client by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arguments?.getParcelable(KEY_CLIENT, Client::class.java)
                ?: error("ClientHistoryFragment should be launched with a client argument")
        else
            arguments?.getParcelable(KEY_CLIENT)
                ?: error("ClientHistoryFragment should be launched with a client argument")
    }

    private val viewModel: ClientHistoryVM by lazy {
        ViewModelProvider(this)[ClientHistoryVM::class.java].apply {
            clientId = client.id
        }
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
                    ClientHistoryScreen(
                        viewModel,
                        this@ClientHistoryFragment::navigateToViewPrintOrderScreen
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enableBackArrow()
        updateTitle(client.name)
        updateSubTitle(getString(R.string.client_history))
        registerBackArrowMenu()
    }

    @ExperimentalComposeUiApi
    @FlowPreview
    private fun navigateToViewPrintOrderScreen(searchModel: SearchModel) {

        viewModel.observePrintOrder(searchModel)

        val args = ComposeViewPrintOrderFragment.getArguments(searchModel.printOrderNumber)
        findNavController().navigate(
            R.id.action_clientHistoryFragment_to_composeViewPrintOrderFragment,
            args
        )
    }
}
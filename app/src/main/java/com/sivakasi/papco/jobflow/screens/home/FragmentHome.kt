package com.sivakasi.papco.jobflow.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.Client
import com.sivakasi.papco.jobflow.extensions.currentUserRole
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.screens.clients.ClientsFragment
import com.sivakasi.papco.jobflow.screens.clients.history.ClientHistoryFragment
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalMaterialApi
@FlowPreview
@ExperimentalComposeUiApi
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class FragmentHome : Fragment() {

    @Inject
    lateinit var auth: JobFlowAuth

    private val viewModel: FragmentHomeVM by lazy {
        ViewModelProvider(this)[FragmentHomeVM::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                HomeScreen(
                    role = currentUserRole(),
                    jobGroups = viewModel.getStates(),
                    findNavController(),
                    this@FragmentHome::signOut
                )
            }
        }
    }

    private fun signOut() = auth.logout()

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onStop() {
        super.onStop()
        showActionBar()
    }


    @OptIn(ExperimentalFoundationApi::class)
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val handle = findNavController().currentBackStackEntry?.savedStateHandle
                handle?.getStateFlow<Client?>(ClientsFragment.KEY_CLIENT, null)
                    ?.collect {
                        it?.let{
                            navigateToClientHistoryScreen(it)
                            handle[ClientsFragment.KEY_CLIENT]=null
                        }
                    }
            }
        }
    }

    private fun navigateToClientHistoryScreen(client: Client) {
        findNavController().navigate(
            R.id.action_fragmentHome_to_clientHistoryFragment,
            ClientHistoryFragment.getArgumentBundle(client)
        )
    }


}
package com.sivakasi.papco.jobflow.screens.home

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
import com.sivakasi.papco.jobflow.extensions.currentUserRole
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.screens.clients.ClientsFragment
import com.sivakasi.papco.jobflow.screens.clients.history.ClientHistoryFragment
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalMaterialApi
@FlowPreview
@ExperimentalComposeUiApi
@AndroidEntryPoint
@ExperimentalCoroutinesApi
class FragmentHome : Fragment() {

    @Inject
    lateinit var auth:JobFlowAuth

    private val viewModel: FragmentHomeVM by lazy {
        ViewModelProvider(this).get(FragmentHomeVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                HomeScreen(
                    role=currentUserRole(),
                    jobGroups = viewModel.getStates(),
                    findNavController(),
                    this@FragmentHome::signOut
                )
            }
        }
    }

    private fun signOut() = auth.logout()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onStop() {
        super.onStop()
        showActionBar()
    }


    private fun observeViewModel() {

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Client>(
            ClientsFragment.KEY_CLIENT
        )?.observe(viewLifecycleOwner) {

            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Client>(
                ClientsFragment.KEY_CLIENT
            )
            navigateToClientHistoryScreen(it)
        }
    }

    private fun navigateToClientHistoryScreen(client: Client) {
        findNavController().navigate(
            R.id.action_fragmentHome_to_clientHistoryFragment,
            ClientHistoryFragment.getArgumentBundle(client)
        )
    }


}
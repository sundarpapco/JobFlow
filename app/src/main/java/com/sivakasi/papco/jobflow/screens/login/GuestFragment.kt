package com.sivakasi.papco.jobflow.screens.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.databinding.ComposeScreenBinding
import com.sivakasi.papco.jobflow.extensions.saveUserRole
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.screens.machines.ManageMachinesFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GuestFragment : Fragment() {

    private var _viewBinding: ComposeScreenBinding? = null
    private val viewBinding: ComposeScreenBinding
        get() = _viewBinding!!

    private val viewModel: GuestFragmentVM by lazy {
        ViewModelProvider(this).get(GuestFragmentVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = ComposeScreenBinding.inflate(inflater, container, false)
        viewBinding.composeView.setContent {
            GuestScreen(
                isLoading = viewModel.isLoading,
                onRefresh = viewModel::onRefresh,
                onSignOut = this::signOut
            )
        }
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        updateTitle(getString(R.string.activation_needed))
        updateSubTitle("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun observeViewModel() {
        viewModel.accountActivated.observe(viewLifecycleOwner) { claim ->

            saveUserRole(claim)
            when (claim) {
                "root" -> {
                    navigateToHomeScreen()
                }

                "admin" -> {
                    navigateToHomeScreen()
                }

                "printer" -> {
                    navigateToMachinesScreen()
                }
            }
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.guestFragment, true).build()
        findNavController().navigate(R.id.action_global_loginFragment, null, navOptions)
    }

    private fun navigateToHomeScreen(){
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.guestFragment, true).build()
        findNavController().navigate(R.id.action_global_fragmentHome, null, navOptions)
    }

    private fun navigateToMachinesScreen(){
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.guestFragment, true)
            .build()
        findNavController().navigate(
            R.id.action_global_manageMachinesFragment,
            ManageMachinesFragment.getArguments(false),
            navOptions
        )
    }
}
package com.sivakasi.papco.jobflow.screens.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.disableBackArrow
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GuestFragment : Fragment() {

    @Inject
    lateinit var auth:JobFlowAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                GuestScreen(
                    onSignOut = this@GuestFragment::logOut
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disableBackArrow()
        updateTitle(getString(R.string.activation_needed))
        updateSubTitle("")
    }

    private fun logOut() = auth.logout()

}
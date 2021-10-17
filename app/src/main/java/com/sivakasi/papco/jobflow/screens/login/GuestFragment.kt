package com.sivakasi.papco.jobflow.screens.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.databinding.ComposeScreenBinding
import com.sivakasi.papco.jobflow.extensions.disableBackArrow
import com.sivakasi.papco.jobflow.extensions.signOut
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GuestFragment : Fragment() {

    private var _viewBinding: ComposeScreenBinding? = null
    private val viewBinding: ComposeScreenBinding
        get() = _viewBinding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = ComposeScreenBinding.inflate(inflater, container, false)
        viewBinding.composeView.setContent {
            GuestScreen(
                onSignOut = this::logOut
            )
        }
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disableBackArrow()
        updateTitle(getString(R.string.activation_needed))
        updateSubTitle("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun logOut() = signOut()

}
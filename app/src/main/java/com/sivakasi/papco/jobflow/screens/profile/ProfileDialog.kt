package com.sivakasi.papco.jobflow.screens.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sivakasi.papco.jobflow.databinding.ComposeScreenBinding
import com.sivakasi.papco.jobflow.extensions.currentUserRole
import com.sivakasi.papco.jobflow.util.JobFlowAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileDialog : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "com.sivakasi.papco.jobflow.ProfileDialog.Tag"
    }

    private var _viewBinding: ComposeScreenBinding? = null
    private val viewBinding: ComposeScreenBinding
        get() = _viewBinding!!

    @Inject
    lateinit var auth: JobFlowAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = ComposeScreenBinding.inflate(inflater, container, false)
        viewBinding.composeView.setContent {
            ProfileScreen(
                name=auth.currentUser?.displayName ?: error("User not logged in"),
                email = auth.currentUser?.email ?: error("User not logged in"),
                role = currentUserRole()
            )
        }
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return viewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
}
@file:Suppress("UNCHECKED_CAST")

package com.sivakasi.papco.jobflow.screens.machines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalFoundationApi
@FlowPreview
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ManageMachinesFragment : Fragment() {

    companion object {
        private const val KEY_SELECT_MODE = "key:selection:mode"
        const val KEY_SELECTED_MACHINE_ID = "key:selected:machine:id"

        fun getArguments(isSelectionMode: Boolean): Bundle {
            return Bundle().apply {
                putBoolean(KEY_SELECT_MODE, isSelectionMode)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {

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

}
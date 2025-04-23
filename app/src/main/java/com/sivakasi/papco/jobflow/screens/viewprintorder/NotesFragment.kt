package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.extensions.hideActionBar
import com.sivakasi.papco.jobflow.extensions.showActionBar
import com.sivakasi.papco.jobflow.screens.notes.NotesScreen
import com.sivakasi.papco.jobflow.ui.JobFlowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NotesFragment : Fragment() {

    companion object {
        private const val KEY_PO_NUMBER = "key:po:number"
        private const val KEY_INITIAL_NOTES = "key:initial:notes"

        fun getArguments(poNumber:Int, initialNotes: String): Bundle =
            Bundle().apply {
                putInt(KEY_PO_NUMBER, poNumber)
                putString(KEY_INITIAL_NOTES, initialNotes)
            }
    }


    private val viewModel: NotesFragmentVM by lazy {
        ViewModelProvider(this)[NotesFragmentVM::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.observePrintOrderForRemoval(getPoNumber(),getInitialNotes())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       return ComposeView(requireContext()).apply {
           setContent {
               JobFlowTheme {
                   NotesScreen(
                       screenState = viewModel.screenState,
                       title = getString(R.string.notes_title,getPoNumber().toString()),
                       onSave= {viewModel.saveNotes()},
                       onClose = {findNavController().popBackStack()}
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

    private fun getPoNumber(): Int =
        arguments?.getInt(KEY_PO_NUMBER) ?: error("PO Number argument not set in notes fragment")

    private fun getInitialNotes(): String =
        arguments?.getString(KEY_INITIAL_NOTES)
            ?: error("Initial notes argument not set in notes fragment")
}
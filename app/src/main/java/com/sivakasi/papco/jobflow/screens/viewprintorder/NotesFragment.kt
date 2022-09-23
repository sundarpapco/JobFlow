package com.sivakasi.papco.jobflow.screens.viewprintorder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.common.ConfirmationDialog
import com.sivakasi.papco.jobflow.common.hideWaitDialog
import com.sivakasi.papco.jobflow.common.showWaitDialog
import com.sivakasi.papco.jobflow.databinding.FragmentNotesBinding
import com.sivakasi.papco.jobflow.extensions.*
import com.sivakasi.papco.jobflow.util.EventObserver
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NotesFragment : Fragment(), ConfirmationDialog.ConfirmationDialogListener {

    companion object {
        private const val KEY_PO_NUMBER = "key:po:number"
        private const val KEY_INITIAL_NOTES = "key:initial:notes"
        private const val KEY_PRESERVE_TEXT_CHANGED = "key:preserve:text:changed"
        private const val CONFIRM_ID_EXIT = 1

        fun getArguments(poNumber:Int, initialNotes: String): Bundle =
            Bundle().apply {
                putInt(KEY_PO_NUMBER, poNumber)
                putString(KEY_INITIAL_NOTES, initialNotes)
            }
    }

    private var textChanged = false
    private var _viewBinding: FragmentNotesBinding? = null
    private val viewBinding: FragmentNotesBinding
        get() = _viewBinding!!

    private val viewModel: NotesFragmentVM by lazy {
        ViewModelProvider(this).get(NotesFragmentVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        textChanged = savedInstanceState?.getBoolean(KEY_PRESERVE_TEXT_CHANGED) ?: false
        viewModel.observePrintOrderForRemoval(getPoNumber())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_notes, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            android.R.id.home -> {
                checkAndExitFragment()
                true
            }
            R.id.mnu_save -> {
                if(textChanged) {
                    val newNotes = viewBinding.txtNotes.text.toString().trim()
                    viewModel.saveNotes(newNotes)
                }else
                    findNavController().popBackStack()
                
                true
            }

            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentNotesBinding.inflate(inflater, container, false)

        if (savedInstanceState == null)
            viewBinding.txtNotes.setText(getInitialNotes())

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableBackArrow()
        updateTitle(getString(R.string.notes_title,getPoNumber().toString()))
        updateSubTitle("")
        initViews()
        observeViewModel()
        registerBackPressedListener{
            checkAndExitFragment()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_PRESERVE_TEXT_CHANGED, textChanged)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun initViews() {
        viewBinding.txtNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                textChanged = true
            }
        })
    }

    private fun observeViewModel() {

        viewModel.saveStatus.observe(viewLifecycleOwner, EventObserver {
            handleSaveStatusEvent(it)
        })

        viewModel.isPrintOrderMovedOrRemoved.observe(viewLifecycleOwner){removedOrMoved->
            if(removedOrMoved)
                showPrintOrderRemovedDialog()
        }

    }

    private fun checkAndExitFragment() {
        if (textChanged)
            showConfirmationDialog()
        else
            findNavController().popBackStack()
    }

    private fun handleSaveStatusEvent(event: LoadingStatus) {

        when (event) {

            is LoadingStatus.Loading -> {
                showWaitDialog(event.msg)
            }

            is LoadingStatus.Success<*> -> {
                hideWaitDialog()
                toast(getString(R.string.notes_saved_successfully))
                findNavController().popBackStack()
            }

            is LoadingStatus.Error -> {
                hideWaitDialog()
                if (event.exception is ResourceNotFoundException)
                    toast(getString(R.string.print_order_not_found))
                else
                    toast(event.exception.message ?: getString(R.string.error_unknown_error))

            }

        }

    }

    private fun showPrintOrderRemovedDialog(){

        val builder=AlertDialog.Builder(requireContext())
        builder.setMessage(getString(R.string.po_not_found_desc))
        builder.setTitle(getString(R.string.po_not_found))
        builder.setPositiveButton(getString(R.string.exit)){_,_->
            findNavController().popBackStack(R.id.composeViewPrintOrderFragment,true)
        }
        builder.setCancelable(false)
        builder.create().show()
    }


    private fun showConfirmationDialog() {
        ConfirmationDialog.getInstance(
            getString(R.string.unsaved_changes_confirmation),
            getString(R.string.exit),
            CONFIRM_ID_EXIT,
            getString(R.string.unsaved_changes_title)
        ).show(childFragmentManager, ConfirmationDialog.TAG)
    }

    override fun onConfirmationDialogConfirm(confirmationId: Int, extra: String) {
        if(confirmationId== CONFIRM_ID_EXIT){
            hideKeyboard(requireContext(),viewBinding.txtNotes)
            findNavController().popBackStack()
        }
    }

    private fun getPoNumber(): Int =
        arguments?.getInt(KEY_PO_NUMBER) ?: error("PO Number argument not set in notes fragment")

    private fun getInitialNotes(): String =
        arguments?.getString(KEY_INITIAL_NOTES)
            ?: error("Initial notes argument not set in notes fragment")
}
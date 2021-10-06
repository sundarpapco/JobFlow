package com.sivakasi.papco.jobflow.screens.machines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestoreException
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.clearErrorOnTextChange
import com.sivakasi.papco.jobflow.databinding.DialogAddMachineBinding
import com.sivakasi.papco.jobflow.extensions.toast
import com.sivakasi.papco.jobflow.util.EventObserver
import com.sivakasi.papco.jobflow.util.LoadingStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddMachineDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "tag:create:machine:dialog"
        private const val KEY_MACHINE_NAME = "key:machine:name"
        private const val KEY_MACHINE_ID = "key:machine:id"

        fun getInstance(
            machineId: String = "",
            machineName: String = ""
        ): AddMachineDialogFragment {
            val args = Bundle().apply {
                putString(KEY_MACHINE_ID, machineId)
                putString(KEY_MACHINE_NAME, machineName)
            }
            return AddMachineDialogFragment().apply {
                arguments = args
            }
        }
    }

    private var _viewBinding: DialogAddMachineBinding? = null
    private val viewBinding: DialogAddMachineBinding
        get() = _viewBinding!!

    private val viewModel: AddMachineVM by lazy {
        ViewModelProvider(this).get(AddMachineVM::class.java)
    }

    override fun onResume() {
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = DialogAddMachineBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        initViews(savedInstanceState)
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun initViews(savedInstanceState: Bundle?) {

        viewBinding.txtName.clearErrorOnTextChange()

        if (savedInstanceState == null && isEditMode())
            viewBinding.txtName.setText(getMachineName())

        viewBinding.btnCancel.setOnClickListener {
            dismiss()
        }

        viewBinding.btnSave.setOnClickListener {
            onButtonSaveClicked()
        }

        viewBinding.lblHeading.text = if (isEditMode())
            getString(R.string.edit_machine)
        else
            getString(R.string.add_machine)
    }

    private fun observeViewModel() {
        viewModel.saveStatus.observe(viewLifecycleOwner, EventObserver {
            handleSaveStatus(it)
        })

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading)
                showProgressBar()
            else
                hideProgressBar()
        }
    }


    private fun onButtonSaveClicked() {
        if (!validateMachineName())
            return

        if (isEditMode())
            viewModel.updateMachine(getMachineId(), viewBinding.txtName.text.toString().trim())
        else
            viewModel.createMachine(viewBinding.txtName.text.toString().trim())
    }

    private fun validateMachineName(): Boolean {

        return if (viewBinding.txtName.text.toString().isBlank()) {
            viewBinding.txtLayoutName.error = getString(R.string.required_field)
            false
        } else
            true
    }

    private fun showProgressBar() {

        //dialog?.setCanceledOnTouchOutside(false)
        viewBinding.txtLayoutName.isEnabled = false
        viewBinding.btnSave.isEnabled = false
        viewBinding.btnCancel.isEnabled = false
        viewBinding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {

        //dialog?.setCanceledOnTouchOutside(true)
        viewBinding.txtLayoutName.isEnabled = true
        viewBinding.btnSave.isEnabled = true
        viewBinding.btnCancel.isEnabled = true
        viewBinding.progressBar.visibility = View.INVISIBLE
    }

    private fun handleSaveStatus(event: LoadingStatus) {

        when (event) {

            is LoadingStatus.Loading -> {

            }

            is LoadingStatus.Success<*> -> {
                dismiss()
            }

            is LoadingStatus.Error -> {

                if (event.exception is FirebaseFirestoreException) {
                    if (event.exception.code == FirebaseFirestoreException.Code.ALREADY_EXISTS)
                        toast(getString(R.string.machine_already_exist))
                    else
                        toast(event.exception.message ?: getString(R.string.error_unknown_error))
                } else
                    toast(event.exception.message ?: getString(R.string.error_unknown_error))
            }

        }
    }

    private fun getMachineName(): String =
        arguments?.getString(KEY_MACHINE_NAME) ?: ""

    private fun getMachineId(): String =
        arguments?.getString(KEY_MACHINE_ID) ?: ""

    private fun isEditMode(): Boolean =
        getMachineId().isNotBlank()
}
@file:Suppress("UNCHECKED_CAST")

package com.sivakasi.papco.jobflow.screens.machines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.common.hideWaitDialog
import com.sivakasi.papco.jobflow.common.showWaitDialog
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.databinding.FragmentMachinesBinding
import com.sivakasi.papco.jobflow.extensions.*
import com.sivakasi.papco.jobflow.screens.destination.FixedDestinationFragment
import com.sivakasi.papco.jobflow.util.EventObserver
import com.sivakasi.papco.jobflow.util.LoadingStatus
import com.sivakasi.papco.jobflow.util.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ManageMachinesFragment : Fragment(), MachinesAdapterListener {

    companion object {
        private const val KEY_SELECT_MODE = "key:selection:mode"
        const val KEY_SELECTED_MACHINE_ID = "key:selected:machine:id"

        fun getArguments(isSelectionMode: Boolean): Bundle {
            return Bundle().apply {
                putBoolean(KEY_SELECT_MODE, isSelectionMode)
            }
        }
    }

    private var _viewBinding: FragmentMachinesBinding? = null
    private val viewBinding: FragmentMachinesBinding
        get() = _viewBinding!!

    private val adapter: MachinesAdapter by lazy {
        MachinesAdapter(this)
    }

    private val viewModel: ManageMachinesVM by lazy {
        ViewModelProvider(this).get(ManageMachinesVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentMachinesBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isPrinterVersionApp())
            disableBackArrow()
        else
            enableBackArrow()

        initViews()
        initRecycler()
        observeViewModel()
        updateFragmentTitle()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initViews() {

        if (isPrinterVersionApp())
            viewBinding.fab.hide()
        else
            viewBinding.fab.setOnClickListener {
                showCreateMachineDialog()
            }
    }

    private fun initRecycler() {
        viewBinding.recycler.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.recycler.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.machines.observe(viewLifecycleOwner) {
            handleListLoadingEvent(it)
        }

        viewModel.deleteStatus.observe(viewLifecycleOwner, EventObserver {
            handleDeleteMachineEvent(it)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.recycler.adapter = null
        _viewBinding = null
    }

    private fun showCreateMachineDialog() {
        AddMachineDialogFragment.getInstance().show(
            childFragmentManager, AddMachineDialogFragment.TAG
        )
    }

    private fun showEditMachineDialog(destination: Destination) {
        AddMachineDialogFragment.getInstance(destination.id, destination.name).show(
            childFragmentManager,
            AddMachineDialogFragment.TAG
        )
    }

    override fun onMachineClicked(machine: Destination) {
        if (isSelectionMode())
            selectMachineAndClose(machine.id)
        else
            navigateToMachineJobListScreen(machine.id)
    }

    override fun onEditMachineClicked(machine: Destination) {
        showEditMachineDialog(machine)
    }

    override fun onDeleteMachineClicked(machine: Destination) {
        viewModel.deleteMachine(machine.id)
    }

    private fun handleListLoadingEvent(event: LoadingStatus) {

        when (event) {
            is LoadingStatus.Loading -> {

            }

            is LoadingStatus.Error -> {
                toast(event.exception.message ?: getString(R.string.error_unknown_error))
            }

            is LoadingStatus.Success<*> -> {
                val list = event.data as List<Destination>
                adapter.submitList(list)
            }
        }

    }

    private fun handleDeleteMachineEvent(event: LoadingStatus) {
        when (event) {
            is LoadingStatus.Loading -> {
                showWaitDialog(event.msg)
            }

            is LoadingStatus.Success<*> -> {
                hideWaitDialog()
            }

            is LoadingStatus.Error -> {
                hideWaitDialog()
                toast(event.exception.message ?: getString(R.string.error_unknown_error))
            }
        }
    }

    private fun navigateToMachineJobListScreen(machineId: String) {
        findNavController().navigate(
            R.id.action_manageMachinesFragment_to_fixedDestinationFragment,
            FixedDestinationFragment.getArgumentBundle(machineId, Destination.TYPE_DYNAMIC)
        )
    }

    private fun selectMachineAndClose(machineId: String) {

        val controller = findNavController()
        controller.previousBackStackEntry?.savedStateHandle?.set(
            KEY_SELECTED_MACHINE_ID,
            machineId
        )
        controller.popBackStack()

    }

    private fun updateFragmentTitle() {
        if (isSelectionMode())
            updateTitle(getString(R.string.select_machine))
        else
            updateTitle(getString(R.string.machines))

        updateSubTitle("")
    }

    private fun isSelectionMode(): Boolean =
        arguments?.getBoolean(KEY_SELECT_MODE) ?: false
}
package com.sivakasi.papco.jobflow.screens.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.DatabaseContract
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.databinding.FixedDestinationBinding
import com.sivakasi.papco.jobflow.databinding.FragmentHomeBinding
import com.sivakasi.papco.jobflow.extensions.disableBackArrow
import com.sivakasi.papco.jobflow.extensions.isPrinterVersionApp
import com.sivakasi.papco.jobflow.extensions.updateSubTitle
import com.sivakasi.papco.jobflow.extensions.updateTitle
import com.sivakasi.papco.jobflow.screens.destination.FixedDestinationFragment
import com.sivakasi.papco.jobflow.screens.machines.ManageMachinesFragment
import com.sivakasi.papco.jobflow.util.Duration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class FragmentHome : Fragment() {

    private var _viewBinding: FragmentHomeBinding? = null
    private val viewBinding: FragmentHomeBinding
        get() = _viewBinding!!

    private val viewModel:FragmentHomeVM by lazy{
        ViewModelProvider(this).get(FragmentHomeVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disableBackArrow()
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        if(isPrinterVersionApp())
            navigateToMachinesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_home,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==R.id.mnu_search){
            findNavController().navigate(R.id.action_fragmentHome_to_searchFragment)
            return true
        }

        if(item.itemId==R.id.mnu_invoice_history){
            findNavController().navigate(R.id.action_fragmentHome_to_invoiceHistoryFragment)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disableBackArrow()
        initViews()
        updateTitle(getString(R.string.papco_jobs))
        updateSubTitle("")
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun initViews() {

        viewBinding.newJobs.icon.setImageResource(R.drawable.ic_new_jobs)
        viewBinding.newJobs.destinationName.text=getString(R.string.new_jobs)
        viewBinding.inProgress.icon.setImageResource(R.drawable.ic_in_progress)
        viewBinding.inProgress.destinationName.text=getString(R.string.in_progress)
        viewBinding.machines.icon.setImageResource(R.drawable.ic_machine)
        viewBinding.machines.destinationName.text=getString(R.string.machines)


        viewBinding.newJobs.root.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentHome_to_fixedDestinationFragment)
        }

        viewBinding.machines.root.setOnClickListener {
            navigateToMachinesFragment()
        }

        viewBinding.inProgress.root.setOnClickListener {
            findNavController().navigate(
                R.id.action_fragmentHome_to_fixedDestinationFragment,
                FixedDestinationFragment.getArgumentBundle(
                    DatabaseContract.DOCUMENT_DEST_IN_PROGRESS,
                    Destination.TYPE_FIXED
                )
            )
        }
    }

    private fun observeViewModel(){
        viewModel.newJobs.observe(viewLifecycleOwner){
            renderDestination(it,viewBinding.newJobs)
        }

        viewModel.inProgress.observe(viewLifecycleOwner){
            renderDestination(it,viewBinding.inProgress)
        }

        viewModel.machines.observe(viewLifecycleOwner){
            renderDestination(it,viewBinding.machines)
        }
    }

    private fun renderDestination(destination:Destination,binding:FixedDestinationBinding){

        with(binding){
            destinationName.text=destination.name
            jobCount.text=getString(R.string.xx_jobs,destination.jobCount)
            runningTime.text=Duration.fromMinutes(destination.runningTime).asFullString()
        }

    }

    private fun navigateToMachinesFragment(){

        if(isPrinterVersionApp()){
            val navOptions=NavOptions.Builder()
                .setPopUpTo(R.id.fragmentHome,true)
                .build()
            findNavController().navigate(
                R.id.action_fragmentHome_to_manageMachinesFragment,
                ManageMachinesFragment.getArguments(false),
                navOptions
            )
        }else{
            findNavController().navigate(
                R.id.action_fragmentHome_to_manageMachinesFragment,
                ManageMachinesFragment.getArguments(false)
            )
        }
    }


}
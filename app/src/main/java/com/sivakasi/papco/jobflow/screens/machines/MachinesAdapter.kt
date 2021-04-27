package com.sivakasi.papco.jobflow.screens.machines

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.common.ListAdapter
import com.sivakasi.papco.jobflow.data.Destination
import com.sivakasi.papco.jobflow.data.DestinationDiff
import com.sivakasi.papco.jobflow.databinding.ListItemDestinationBinding
import com.sivakasi.papco.jobflow.extensions.isPrinterVersionApp
import com.sivakasi.papco.jobflow.util.Duration

class MachinesAdapter(private val callback: MachinesAdapterListener) :
    ListAdapter<Destination, MachineViewHolder>(DestinationDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MachineViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewBinding = ListItemDestinationBinding.inflate(inflater, parent, false)
        val viewHolder = MachineViewHolder(viewBinding)
        viewBinding.root.setOnClickListener {
            callback.onMachineClicked(getData(viewHolder.bindingAdapterPosition))
        }

        if(isPrinterVersionApp())
            viewBinding.iconMore.visibility=View.GONE
        else {
            viewBinding.iconMore.visibility=View.VISIBLE
            viewBinding.iconMore.setOnClickListener {
                showPopupMenu(it, viewHolder.bindingAdapterPosition)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: MachineViewHolder, position: Int) {
        holder.bind(getData(holder.bindingAdapterPosition))
    }

    private fun showPopupMenu(view:View,position: Int){
        val menu=PopupMenu(view.context,view)
        menu.inflate(R.menu.edit_delete_machine)
        menu.setOnMenuItemClickListener {
            if(it.itemId==R.id.mnu_edit_machine) {
                callback.onEditMachineClicked(getData(position))
            }

            if(it.itemId==R.id.mnu_delete_machine)
                callback.onDeleteMachineClicked(getData(position))

            true
        }
        menu.show()
    }
}


class MachineViewHolder(private val viewBinding: ListItemDestinationBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {

    fun bind(destination: Destination) {
        val duration = Duration.fromMinutes(destination.runningTime).toString()
        viewBinding.lblName.text = destination.name
        viewBinding.lblDuration.text = viewBinding.root.context.getString(
            R.string.duration_in_xx_jobs,
            duration,
            destination.jobCount
        )
    }
}

interface MachinesAdapterListener{
    fun onMachineClicked(machine:Destination)
    fun onMachineLongClicked(machine:Destination){}
    fun onEditMachineClicked(machine: Destination)
    fun onDeleteMachineClicked(machine:Destination)
}
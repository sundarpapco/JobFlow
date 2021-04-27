package com.sivakasi.papco.jobflow.screens.destination

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.common.ItemTouchHelperCallBack
import com.sivakasi.papco.jobflow.common.JobListSelection
import com.sivakasi.papco.jobflow.common.ListAdapter
import com.sivakasi.papco.jobflow.common.ListAdapterListener
import com.sivakasi.papco.jobflow.databinding.ListItemJobBinding
import com.sivakasi.papco.jobflow.models.PrintOrderUIModel
import com.sivakasi.papco.jobflow.models.PrintOrderUIModelDiff

class JobsAdapter(
    private val context: Context,
    private val selections: JobListSelection,
    private val callback: ListAdapterListener<PrintOrderUIModel>
) :
    ListAdapter<PrintOrderUIModel, JobListViewHolder>(PrintOrderUIModelDiff()),
    ItemTouchHelperCallBack.DragCallBack {

    var itemTouchHelper: ItemTouchHelper? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobListViewHolder {
        val viewBinding =
            ListItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val viewHolder = JobListViewHolder(context,viewBinding)
        viewBinding.root.setOnClickListener {
            callback.onItemClick(
                getData(viewHolder.bindingAdapterPosition),
                viewHolder.bindingAdapterPosition
            )
        }

        viewBinding.iconDrag.setOnTouchListener { _, motionEvent ->

            if (selections.size() > 0)
                return@setOnTouchListener true

            if (motionEvent.action == MotionEvent.ACTION_DOWN)
                itemTouchHelper?.startDrag(viewHolder)

            true
        }

        viewBinding.root.setOnLongClickListener {
            callback.onItemLongClick(
                getData(viewHolder.bindingAdapterPosition),
                viewHolder.bindingAdapterPosition
            )
            true
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: JobListViewHolder, position: Int) {

        val item = getData(holder.bindingAdapterPosition)
        holder.bind(item, selections.contains(item.printOrderNumber))
    }

    override fun onDragging(fromPosition: Int, toPosition: Int) {

        val from = getData(fromPosition)
        val to = getData(toPosition)
        val fromListPosition = from.listPosition
        from.listPosition = to.listPosition
        to.listPosition = fromListPosition

        setData(fromPosition, to)
        setData(toPosition, from)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        val updatingData = if (fromPosition > toPosition)
            getSubData(toPosition, fromPosition + 1)
        else
            getSubData(fromPosition, toPosition + 1)
        callback.onItemMoved(updatingData)
    }
}


class JobListViewHolder(
    private val context: Context,
    private val viewBinding: ListItemJobBinding
) : RecyclerView.ViewHolder(viewBinding.root) {

    private val accentColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val borderColor = ContextCompat.getColor(context, R.color.border_grey)

    fun bind(printOrderModel: PrintOrderUIModel, isSelected: Boolean) {

        with(printOrderModel) {
            viewBinding.lblPoNumber.text = poNumberAndAge
            viewBinding.iconStar.visibility = if (emergency)
                View.VISIBLE else View.GONE
            viewBinding.lblReprint.visibility = if (isReprint)
                View.VISIBLE else View.GONE
            viewBinding.lblClientName.text = billingName
            viewBinding.lblJobName.text = jobName
            viewBinding.lblTime.text = runningTime.timeFormatString()
            viewBinding.lblPaperDetail.text = printingSizePaperDetail
            viewBinding.lblColors.text = colors

            if(hasSpotColors)
                viewBinding.lblColors.setTextColor(accentColor)
            else
                viewBinding.lblColors.setTextColor(borderColor)

            if(isPending()){
                viewBinding.iconPending.visibility=View.VISIBLE
                viewBinding.iconPending.setOnClickListener {
                    Toast.makeText(context, pendingReason, Toast.LENGTH_SHORT).show()
                }
            }else {
                viewBinding.iconPending.setOnClickListener(null)
                viewBinding.iconPending.visibility = View.GONE
            }

            viewBinding.iconPending.visibility = if (isPending()) {
                View.VISIBLE
            }else {
                View.GONE
            }
        }

        viewBinding.root.isActivated = isSelected

    }

}
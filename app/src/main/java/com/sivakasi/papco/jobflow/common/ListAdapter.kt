package com.sivakasi.papco.jobflow.common

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.util.*

abstract class ListAdapter<T, VH : RecyclerView.ViewHolder>(
    private val diffItemCallBack: DiffUtil.ItemCallback<T>
) : RecyclerView.Adapter<VH>() {

    private var diffUtilJob: Job? = null
    private var data: MutableList<T> = LinkedList()

    override fun getItemCount(): Int {
        return data.size
    }

    protected fun getData(position: Int): T = data[position]

    protected fun setData(position: Int, data: T) {
        this.data[position] = data
    }

    protected fun getSubData(fromPosition: Int, toPosition: Int) =
        data.subList(fromPosition, toPosition)

    fun submitList(newData: List<T>) = runDiffUtilAndUpdateList(newData)


    @OptIn(DelicateCoroutinesApi::class)
    private fun runDiffUtilAndUpdateList(newData: List<T>) {

        diffUtilJob?.cancel() //cancel any previously running job

        diffUtilJob = GlobalScope.launch(Dispatchers.Default) {
            val diffResult = DiffUtil.calculateDiff(createDiffCallBackFor(newData))
            withContext(Dispatchers.Main) {
                yield()
                data = newData.toMutableList()
                diffResult.dispatchUpdatesTo(this@ListAdapter)
            }
        }
    }


    private fun createDiffCallBackFor(newData: List<T>): DiffUtil.Callback {

        return object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                diffItemCallBack.areItemsTheSame(
                    data[oldItemPosition]!!,
                    newData[newItemPosition]!!
                )

            override fun getOldListSize(): Int = data.size

            override fun getNewListSize(): Int = newData.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                diffItemCallBack.areContentsTheSame(
                    data[oldItemPosition]!!,
                    newData[newItemPosition]!!
                )

        }
    }
}


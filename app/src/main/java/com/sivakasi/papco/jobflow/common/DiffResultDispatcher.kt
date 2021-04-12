package com.sivakasi.papco.jobflow.common

import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView

/*

This class is supposed to be used in the RecyclerView adapter which uses Spacing decoration to add
extra spacing at bottom and also uses DiffUtils to update its contents. Usually while dispatching the
DiffResult, it wont change the bottom spacing of the already last item while adding a new item as last.
Instead of dispatching the DiffResult directly to adapter, use this class and dispatch the result to this class
instead.

This class will detect if the item being inserted or removed is the last item of the list. If so,
it will notify the previous item of the list also to update cause the previously last item will have
a large spacing in the bottom spacing cause of item decoration and it needs to change.

 */


class DiffResultDispatcher<VH:RecyclerView.ViewHolder>(
    private val oldDataSize:Int,
    private val adapter: RecyclerView.Adapter<VH>
) : ListUpdateCallback {

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        adapter.notifyItemRangeChanged(position, count, payload)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        adapter.notifyItemMoved(fromPosition, toPosition)
    }

    override fun onInserted(position: Int, count: Int) {

        if (oldDataSize>0 && oldDataSize == position)
            adapter.notifyItemChanged(position - 1)

        adapter.notifyItemRangeInserted(position, count)

    }

    override fun onRemoved(position: Int, count: Int) {

        if (position > 0 && (position + count) == oldDataSize) {
            adapter.notifyItemChanged(position - 1, Boolean)
        }

        adapter.notifyItemRangeRemoved(position, count)

    }


}
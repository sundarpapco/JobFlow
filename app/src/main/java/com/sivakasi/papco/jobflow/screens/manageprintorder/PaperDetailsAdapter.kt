package com.sivakasi.papco.jobflow.screens.manageprintorder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.data.PaperDetail
import com.sivakasi.papco.jobflow.data.PaperDetailDiff
import com.sivakasi.papco.jobflow.databinding.AddPaperDetailBinding
import com.sivakasi.papco.jobflow.databinding.ListItemPaperDetailBinding

class PaperDetailsAdapter(
   private val callback:PaperDetailAdapterListener
) :
    ListAdapter<PaperDetail, PaperDetailsAdapter.PaperDetailVH>(PaperDetailDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaperDetailVH {
        val binding = ListItemPaperDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PaperDetailVH(binding,callback)
    }

    override fun onBindViewHolder(holder: PaperDetailVH, position: Int) {
        holder.bind(getItem(holder.bindingAdapterPosition))
    }

    class PaperDetailVH(
        private val viewBinding: ListItemPaperDetailBinding,
        private val callback:PaperDetailAdapterListener) :
        RecyclerView.ViewHolder(viewBinding.root) {

        private val context = viewBinding.root.context
        private lateinit var data:PaperDetail

        init {
            viewBinding.icDelete.setOnClickListener {
                callback.onDeletePaperDetail(bindingAdapterPosition)
            }

            viewBinding.root.setOnClickListener {
                callback.onEditPaperDetail(bindingAdapterPosition,data)
            }
        }

        fun bind(data: PaperDetail) {
            this.data=data
            val owner = if (data.partyPaper)
                context.getString(R.string.party_own)
            else
                context.getString(R.string.our_own)

            viewBinding.lblPaperOwner.text = owner
            viewBinding.lblPaperName.text = data.toString()
        }
    }

    interface PaperDetailAdapterListener{
        fun onEditPaperDetail(index:Int,paperDetail: PaperDetail)
        fun onDeletePaperDetail(index:Int)
    }
}

class AddPaperDetailAdapter(
    private val callback: CallBack
) : RecyclerView.Adapter<AddPaperDetailAdapter.AddPaperVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddPaperVH {

        val inflater = LayoutInflater.from(parent.context)
        val binding = AddPaperDetailBinding.inflate(inflater, parent, false)
        return AddPaperVH(binding,callback)
    }

    override fun onBindViewHolder(holder: AddPaperVH, position: Int) {
        // Do nothing
    }

    override fun getItemCount(): Int = 1

    class AddPaperVH(
        viewBinding: AddPaperDetailBinding,
        private val callback: CallBack
    ) : RecyclerView.ViewHolder(viewBinding.root){

        init {
            viewBinding.root.setOnClickListener {
                callback.onAddPaperDetail()
            }
        }

    }

    interface CallBack {
        fun onAddPaperDetail()
    }
}
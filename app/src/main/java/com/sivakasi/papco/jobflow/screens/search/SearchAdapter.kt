package com.sivakasi.papco.jobflow.screens.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sivakasi.papco.jobflow.databinding.ListItemSearchBinding
import com.sivakasi.papco.jobflow.models.SearchModel

class SearchAdapter(
    private val callback: SearchAdapterListener
) : RecyclerView.Adapter<SearchViewHolder>() {

    private var data:List<SearchModel> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding =
            ListItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return SearchViewHolder(binding,callback)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(getData(holder.bindingAdapterPosition))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun getData(position:Int):SearchModel=
        data[position]

    fun submitList(newData:List<SearchModel>){
        data=newData
        notifyDataSetChanged()
    }
}

class SearchViewHolder(
    private val binding: ListItemSearchBinding,
    callback: SearchAdapterListener
) : RecyclerView.ViewHolder(binding.root) {

    private var item:SearchModel?=null

    init {
        binding.root.setOnClickListener {
            item?.let{
                callback.onItemClick(it)
            }
        }
    }

    fun bind(searchModel: SearchModel) {
        item=searchModel
        binding.lblPoNumber.text = searchModel.poNumberAndDate()
        binding.lblRid.text = searchModel.rid()
        binding.lblClientName.text = searchModel.billingName
        binding.lblJobName.text = searchModel.jobName
        binding.lblInvoiceNumber.text = searchModel.invoiceNumber
        binding.lblColors.text = searchModel.colors
        binding.lblPaperDetail.text = searchModel.paperDetails
        binding.lblStatus.text=searchModel.status()
    }
}

interface SearchAdapterListener {
    fun onItemClick(item: SearchModel)
}